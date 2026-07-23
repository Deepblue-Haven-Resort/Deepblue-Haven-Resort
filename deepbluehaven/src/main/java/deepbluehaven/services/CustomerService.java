package deepbluehaven.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import deepbluehaven.dto.ServiceDTO;
import deepbluehaven.pojo.enums.ServiceStatus;
import deepbluehaven.repositories.ServiceRepository;

@Service
public class CustomerService {
    private final ServiceRepository serviceRepository;

    public CustomerService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public List<ServiceDTO.Response> getAllActiveServices() {
        List<deepbluehaven.pojo.Service> entities = serviceRepository.findByStatus(ServiceStatus.ACTIVE);
        return entities.stream().map(this::toResponse).toList();
    }

    public List<ServiceDTO.Response> getAllOutOfStockServices() {
        List<deepbluehaven.pojo.Service> entities = serviceRepository.findByStatus(ServiceStatus.OUT_OF_STOCK);
        return entities.stream().map(this::toResponse).toList();
    }

    public List<ServiceDTO.Response> getVisibleServices() {
        List<ServiceDTO.Response> result = new ArrayList<>();
        result.addAll(getAllActiveServices());
        result.addAll(getAllOutOfStockServices());
        return result;
    }

    private ServiceDTO.Response toResponse(deepbluehaven.pojo.Service entity) {
    ServiceDTO.Response dto = new ServiceDTO.Response();
    dto.setId(entity.getId());
    dto.setName(entity.getName());
    dto.setDescription(entity.getDescription());
    dto.setType(entity.getType());
    dto.setCategory(entity.getCategory());
    dto.setBasePrice(entity.getBasePrice());
    dto.setUnit(entity.getUnit());
    dto.setStatus(entity.getStatus());
    boolean isAvailable = (entity.getStatus() != null && entity.getStatus().name().equals("ACTIVE"));
    dto.setStatusClass(isAvailable ? "service-card__status--available" : "service-card__status--out-of-stock");
    dto.setStatusValue(isAvailable ? "active" : "out-of-stock");
    return dto;
}
}
