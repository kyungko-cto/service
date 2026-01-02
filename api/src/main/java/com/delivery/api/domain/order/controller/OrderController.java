@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody CreateOrderRequest request) {

        // 리팩토링: create 시점에 생성된 엔티티를 바로 반환하여 추가 select 제거
        OrderResponse response = orderService.createOrderOptimized(
                user.getId(),
                request.getStoreId(),
                request.getItems().stream().map(OrderConverter::toDomainItem).toList());

        return ApiResponse.success(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> get(@PathVariable UUID orderId) {
        // 가상 스레드 환경에서 DB 조회 병목 최소화
        return ApiResponse.success(orderService.getOrderDetails(orderId));
    }
}