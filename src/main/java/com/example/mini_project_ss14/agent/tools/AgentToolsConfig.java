package com.example.mini_project_ss14.agent.tools;

import com.example.mini_project_ss14.agent.entity.Delivery;
import com.example.mini_project_ss14.agent.entity.Incident;
import com.example.mini_project_ss14.agent.repository.DeliveryRepository;
import com.example.mini_project_ss14.agent.repository.IncidentRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration
public class AgentToolsConfig {

    private final IncidentRepository incidentRepository;
    private final DeliveryRepository deliveryRepository;

    public AgentToolsConfig(IncidentRepository incidentRepository, DeliveryRepository deliveryRepository) {
        this.incidentRepository = incidentRepository;
        this.deliveryRepository = deliveryRepository;
    }

    @Bean
    @Description("Tạo phiếu sự cố (hỏng hóc, giao trễ, thất lạc).")
    public Function<CreateIncidentRequest, String> createIncidentTool() {
        return request -> {
            Delivery delivery = deliveryRepository.findByTrackingCode(request.trackingCode()).orElse(null);
            
            if (delivery == null) {
                return "Lỗi: Không tìm thấy đơn hàng " + request.trackingCode();
            }

            Incident incident = new Incident();
            incident.setTrackingCode(request.trackingCode());
            incident.setIncidentType(request.incidentType());
            incident.setHubCode(request.hubCode());
            incident.setSeverity(request.severity());
            incident.setDescription(request.description());
            incidentRepository.save(incident);

            return "Đã tạo phiếu sự cố thành công!";
        };
    }

    @Bean
    @Description("Cập nhật trạng thái đơn hàng (DAMAGED, DELAYED).")
    public Function<UpdateDeliveryStatusRequest, String> updateDeliveryStatusTool() {
        return request -> {
            Delivery delivery = deliveryRepository.findByTrackingCode(request.trackingCode()).orElse(null);
            
            if (delivery == null) {
                return "Lỗi: Không tìm thấy đơn hàng " + request.trackingCode();
            }

            delivery.setStatus(request.newStatus());
            deliveryRepository.save(delivery);

            return "Đã cập nhật trạng thái thành công!";
        };
    }
}
