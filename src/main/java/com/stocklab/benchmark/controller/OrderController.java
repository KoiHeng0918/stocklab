package com.stocklab.benchmark.controller;

import com.stocklab.benchmark.entity.Order;
import com.stocklab.benchmark.exception.BusinessException;
import com.stocklab.benchmark.service.OrderService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 只做三件事：解析请求、调用 Service、返回结果；业务逻辑一律不写在这里。 */
@RestController
@RequestMapping("/order")
@Validated
public class OrderController {

    @Autowired

    private OrderService orderService;



    @PostMapping("/create")
    public Order create(
            @RequestParam
            @NotBlank(message = "requestId 不能为空")
            @Size(max = 64, message = "requestId 长度不能超过 64")
            String requestId,

            @RequestParam(defaultValue = "1")
            @Positive(message = "skuId 必须大于 0")
            Long skuId) {
        return orderService.create(requestId, skuId);
    }

    @GetMapping("/{requestId}")
    public Order getByRequestId(
            @PathVariable
            @NotBlank(message = "requestId 不能为空")
            @Size(max = 64, message = "requestId 长度不能超过 64")
            String requestId) {
        Order order = orderService.getByRequestId(requestId);
        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "订单不存在");
        }
        return order;
    }

    @PostMapping("/{id}/cancel")
    public Order cancel(
            @PathVariable
            @Positive(message = "id 必须大于 0")
            Long id) {
        return orderService.cancel(id);
    }



}
