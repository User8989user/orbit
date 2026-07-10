@RestController
@RequestMapping("/api/v1/payments/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<?> createAccount(@RequestHeader("X-User-Id") String userId) {
        try {
            Account account = accountService.createAccount(userId);
            return ResponseEntity.ok(Map.of("account_id", account.getId(), "user_id", account.getUserId()));
        } catch (AccountAlreadyExistsException e) {
            return ResponseEntity.status(409).body(errorResponse("ACCOUNT_ALREADY_EXISTS", e.getMessage()));
        }
    }

    @PostMapping("/top-up")
    public ResponseEntity<?> topUp(@RequestHeader("X-User-Id") String userId,
                                   @Valid @RequestBody TopUpRequest request) {
        if (request.getAmount() <= 0) {
            return ResponseEntity.badRequest().body(errorResponse("INVALID_AMOUNT", "Amount must be > 0"));
        }
        try {
            Account account = accountService.topUp(userId, request.getAmount());
            return ResponseEntity.ok(Map.of("user_id", account.getUserId(), "balance", account.getBalance(), "currency", "geocredits"));
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(404).body(errorResponse("ACCOUNT_NOT_FOUND", e.getMessage()));
        }
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@RequestHeader("X-User-Id") String userId) {
        try {
            Account account = accountService.getBalance(userId);
            return ResponseEntity.ok(Map.of("user_id", account.getUserId(), "balance", account.getBalance(), "currency", "geocredits"));
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(404).body(errorResponse("ACCOUNT_NOT_FOUND", e.getMessage()));
        }
    }

    private Map<String, Object> errorResponse(String code, String message) {
        return Map.of("error_code", code, "message", message, "timestamp", Instant.now().toString());
    }
}