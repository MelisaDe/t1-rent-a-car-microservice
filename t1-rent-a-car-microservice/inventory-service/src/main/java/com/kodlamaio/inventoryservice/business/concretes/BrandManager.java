package com.kodlamaio.inventoryservice.business.concretes;

import com.kodlamaio.commonpackage.events.inventory.BrandDeletedEvent;
import com.kodlamaio.commonpackage.utils.kafka.producer.KafkaProducer;
import com.kodlamaio.commonpackage.utils.mappers.ModelMapperService;
import com.kodlamaio.inventoryservice.business.abstracts.BrandService;
import com.kodlamaio.inventoryservice.business.dto.requests.create.CreateBrandRequest;
import com.kodlamaio.inventoryservice.business.dto.requests.update.UpdateBrandRequest;
import com.kodlamaio.inventoryservice.business.dto.responses.create.CreateBrandResponse;
import com.kodlamaio.inventoryservice.business.dto.responses.get.GetAllBrandsResponse;
import com.kodlamaio.inventoryservice.business.dto.responses.get.GetBrandResponse;
import com.kodlamaio.inventoryservice.business.dto.responses.update.UpdateBrandResponse;
import com.kodlamaio.inventoryservice.business.rules.BrandBusinessRules;
import com.kodlamaio.inventoryservice.entities.Brand;
import com.kodlamaio.inventoryservice.repository.BrandRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BrandManager implements BrandService {
    private final BrandRepository repository;
    private final ModelMapperService mapper;
    private final BrandBusinessRules rules;
    private final KafkaProducer producer;
    @Override
    public List<GetAllBrandsResponse> getAll() {
        var brands = repository.findAll();
        var responses = brands
                .stream()
                .map(brand -> mapper.forResponse().map(brand, GetAllBrandsResponse.class))
                .toList();
        return responses;
    }

    @Override
    public GetBrandResponse getById(UUID id) {
        rules.checkIfBrandExistsById(id);
        var brand = repository.findById(id).orElseThrow();
        var response = mapper.forResponse().map(brand, GetBrandResponse.class);
        return response;
    }

    @Override
    public CreateBrandResponse add(CreateBrandRequest request) {
        rules.checkIfBrandExistsByName(request.getName());
        var brand = mapper.forResponse().map(request, Brand.class);
        brand.setId(UUID.randomUUID());
        repository.save(brand);
        var response = mapper.forResponse().map(brand, CreateBrandResponse.class);
        return response;
    }

    @Override
    public UpdateBrandResponse update(UUID id, UpdateBrandRequest request) {
        rules.checkIfBrandExistsById(id);
        var brand = mapper.forResponse().map(request, Brand.class);
        brand.setId(id);
        repository.save(brand);
        var response = mapper.forResponse().map(brand, UpdateBrandResponse.class);
        return response;
    }

    @Override
    public void delete(UUID id) {
        rules.checkIfBrandExistsById(id);
        repository.deleteById(id);
        sendKafkaBrandDeletedEvent(id);
    }

    private void sendKafkaBrandDeletedEvent(UUID id) {
        producer.sendMessage(new BrandDeletedEvent(id), "brand-deleted");
    }
}
