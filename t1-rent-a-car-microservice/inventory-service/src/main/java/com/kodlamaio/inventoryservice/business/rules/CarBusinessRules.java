package com.kodlamaio.inventoryservice.business.rules;

import com.kodlamaio.commonpackage.utils.exceptions.BusinessException;
import com.kodlamaio.inventoryservice.entities.enums.State;
import com.kodlamaio.inventoryservice.repository.CarRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class CarBusinessRules {
    private final CarRepository repository;

    public void checkIfCarExistsById(UUID id) {
        if (!repository.existsById(id)) throw new BusinessException("Car Not Exists");//BusinessException(Messages.Car.NotExists);
    }
    public void checkIfCarExistsByPlate(String plate) {
        if (repository.existsByPlateIgnoreCase(plate))
            throw new BusinessException("Plate Exists");//BusinessException(Messages.Car.PlateExists);
    }
    public void checkCarAvailability(UUID id) {
        var car = repository.findById(id).orElseThrow();
        if (!car.getState().equals(State.Avaılable))
            throw new BusinessException("CAR_NOT_AVAILABLE");//BusinessException(Messages.Car.PlateExists);
    }
}
