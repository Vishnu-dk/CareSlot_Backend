package com.careslot.service;

import com.careslot.db.generated.tables.records.AppointmentsRecord;
import com.careslot.db.generated.tables.records.CliniciansRecord;
import com.careslot.db.generated.tables.records.UsersRecord;
import com.careslot.dto.appointment.PatientAppointmentResponse;
import com.careslot.dto.clinician.ClinicianProfileRequest;
import com.careslot.dto.clinician.ClinicianResponse;
import com.careslot.dto.patient.PatientResponse;
import com.careslot.exception.ResourceNotFoundException;
import com.careslot.exception.UserAlreadyExistsException;
import com.careslot.repository.AppointmentRepository;
import com.careslot.repository.ClinicianRepository;
import com.careslot.repository.PatientRepository;
import com.careslot.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClinicianService {

    private final ClinicianRepository clinicianRepo;
    private final UserRepository userRepo;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;

    public ClinicianService(ClinicianRepository clinicianRepo, UserRepository userRepo, AppointmentRepository appointmentRepository, PatientRepository patientRepository) {
        this.clinicianRepo = clinicianRepo;
        this.userRepo = userRepo;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
    }

    public void createProfile(UUID userId, ClinicianProfileRequest request) {
        if (userRepo.findById(userId).isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }
        if (clinicianRepo.existsById(userId)) {
            throw new UserAlreadyExistsException("Clinician profile already exists for this user");
        }
        Optional<CliniciansRecord> record= clinicianRepo.findById(userId);

        boolean isSameLicense = clinicianRepo.findById(userId)
                .map(CliniciansRecord::getLicenseNumber)
                .filter(license -> license.equalsIgnoreCase(request.getLicenseNumber()))
                .isPresent();

        if(isSameLicense){
            throw new UserAlreadyExistsException("Licence number exists");
        }

        clinicianRepo.saveProfile(userId, request.getFirstName(), request.getLastName(), request.getSpecialty(), request.getLicenseNumber());
    }

    public List<ClinicianResponse> getAllClinicians() {
        return clinicianRepo.findAll().stream()
                .map(p -> {
                    Optional<UsersRecord> userRecord = userRepo.findAllUserById(p.getId());
                    String email = null;
                    LocalDate deletedAt=null;

                    if (userRecord.isPresent()) {
                        email = userRecord.get().getEmail();

                        deletedAt = userRecord
                                .map(UsersRecord::getDeletedAt)
                                .map(OffsetDateTime::toLocalDate)
                                .orElse(null);                }


                    return ClinicianResponse.builder()
                            .userId(p.getId())
                            .firstName(p.getFirstName())
                            .lastName(p.getLastName())
                            .specialty(p.getSpecialty())
                            .email(email)
                            .deletedAt(deletedAt)
                            .build();
                })
                .collect(Collectors.toList());

    }

    public List<PatientAppointmentResponse> getMyPatients(UUID userId) {
        return appointmentRepository.findByClinicianId(userId).stream()

                .map(appointment->{
                    UUID patientId=appointment.getPatientId();

                    return patientRepository.findByUserId(patientId)
                            .map(patientRecord -> PatientAppointmentResponse.builder()

                                    .userId(patientRecord.getUserId())
                                    .firstName(patientRecord.getFirstName())
                                    .lastName(patientRecord.getLastName())
                                    .dateOfBirth(patientRecord.getDateOfBirth())
                                    .phoneNumber(patientRecord.getPhoneNumber())

                                    .status(appointment.getStatus())
                                    .lastVisit(appointment.getStartsAt().toLocalDate())
                                    .build())
                            .orElse(null);
                })

                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}