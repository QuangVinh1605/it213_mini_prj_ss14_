package com.example.mini_project_ss14.mcp.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class DeliveryLookupTool {

    @Tool(
            name = "get_delivery_by_id",
            description = "Get delivery information by delivery ID"
    )
    public String getDeliveryById(
            @ToolParam(description = "The ID of the delivery") Long deliveryId) {

        return "Delivery " + deliveryId + " found. Status: IN_TRANSIT";
    }
}