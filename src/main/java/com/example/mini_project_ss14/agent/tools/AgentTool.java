package com.example.mini_project_ss14.agent.tools;

import com.example.mini_project_ss14.agent.entity.Delivery;
import com.example.mini_project_ss14.agent.entity.Incident;
import com.example.mini_project_ss14.agent.repository.DeliveryRepository;
import com.example.mini_project_ss14.agent.repository.IncidentRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

@Service
public class AgentTool {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Tool
    @Description("Tạo phiếu sự cố (hỏng hóc, giao trễ, thất lạc). Cần truyền vào mã vận đơn, loại sự cố, bưu cục, mức độ và mô tả.")
    public String createIncidentTool(CreateIncidentRequest request) {
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
    }

    @Tool
    @Description("Cập nhật trạng thái đơn hàng. Cần truyền vào mã vận đơn và trạng thái mới (DAMAGED, DELAYED).")
    public String updateDeliveryStatusTool(UpdateDeliveryStatusRequest request) {
        Delivery delivery = deliveryRepository.findByTrackingCode(request.trackingCode()).orElse(null);

        if (delivery == null) {
            return "Lỗi: Không tìm thấy đơn hàng " + request.trackingCode();
        }

        delivery.setStatus(request.newStatus());
        deliveryRepository.save(delivery);

        return "Đã cập nhật trạng thái thành công!";
    }
}
