package com.example.jms.wsd1.api.v3;

import com.example.jms.wsd1.api.dto.ItemDto;
import com.example.jms.wsd1.api.request.ItemCreateRequest;
import com.example.jms.wsd1.api.request.ItemPriceUpdateRequest; // [추가]
import com.example.jms.wsd1.api.request.ItemUpdateRequest;
import com.example.jms.wsd1.common.ApiException;   // [추가]
import com.example.jms.wsd1.common.ApiResponse;    // [추가]
import com.example.jms.wsd1.common.ErrorCode;      // [추가]
import jakarta.validation.Valid;                      // [추가]
import jakarta.validation.constraints.Max;            // [추가]
import jakarta.validation.constraints.Min;            // [추가]
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;                                  // [추가]
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;                       // [추가]
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v3/items")
public class ItemController3 {

    // 메모리용 임시 DB (예제용)
    private final Map<Long, ItemDto> store = new HashMap<>();
    private long sequence = 1L;

    // 1) GET: 전체 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemDto>>> getItems() {
        return ResponseEntity.ok(ApiResponse.success(new ArrayList<>(store.values())));
    }

    // 1) GET: 단건 조회 (PathVariable 사용)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemDto>> getItem(@PathVariable Long id) {
        ItemDto item = findItemOrThrow(id);
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    // 1) POST: 생성
    @PostMapping
    public ResponseEntity<ApiResponse<ItemDto>> createItem(@Valid @RequestBody ItemCreateRequest request) {
        ItemDto item = new ItemDto();
        item.setId(sequence++);
        item.setName(request.getName());
        item.setPrice(request.getPrice());

        store.put(item.getId(), item);

        return ResponseEntity.created(URI.create("/api/v3/items/" + item.getId()))
                .body(ApiResponse.success(item));
    }

    // 1) PUT: 수정
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemDto>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemUpdateRequest request
    ) {
        ItemDto item = findItemOrThrow(id);

        if (request.getName() == null && request.getPrice() == null) {
            throw new ApiException(ErrorCode.EMPTY_UPDATE);
        }

        if (request.getName() != null) {
            item.setName(request.getName());
        }
        if (request.getPrice() != null) {
            item.setPrice(request.getPrice());
        }

        return ResponseEntity.ok(ApiResponse.success(item));
    }

    // 1) DELETE: 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemDto>> deleteItem(@PathVariable Long id) {
        ItemDto removed = store.remove(id);
        if (removed == null) {
            throw new ApiException(ErrorCode.ITEM_NOT_FOUND);
        }

        return ResponseEntity.ok(ApiResponse.success(removed));
    }

    // /api/items/search?keyword=abc&page=1&size=10
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ItemDto>>> searchItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        // 예제: keyword 포함된 name만 필터링
        List<ItemDto> result = store.values().stream()
                .filter(item -> keyword == null || item.getName().contains(keyword))
                .skip((long) page * size)
                .limit(size)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // 헤더를 읽으면서 아이템을 등록
    // 원본은 요청 내용과 헤더 값을 문자열로 돌려주기만 하고 저장하지 않았다.
    @PostMapping("/with-header")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createItemWithHeader(
            @Valid @RequestBody ItemCreateRequest request,
            @RequestHeader(value = "X-USER-ID", required = false) String userId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        ItemDto item = new ItemDto();
        item.setId(sequence++);
        item.setName(request.getName());
        item.setPrice(request.getPrice());

        store.put(item.getId(), item);

        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("X-USER-ID", userId);
        headers.put("Authorization", authorization);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("item", item);
        data.put("headers", headers);

        return ResponseEntity.created(URI.create("/api/v3/items/" + item.getId()))
                .body(ApiResponse.success(data));
    }

    //PUT: 가격만 수정
    @PutMapping("/{id}/price")
    public ResponseEntity<ApiResponse<ItemDto>> updatePrice(
            @PathVariable Long id,
            @Valid @RequestBody ItemPriceUpdateRequest request
    ) {
        ItemDto item = findItemOrThrow(id);
        item.setPrice(request.getPrice());
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    //DELETE: 전체 삭제 (실수 방지를 위해 confirm=true 필요, 없으면 400)
    @DeleteMapping
    public ResponseEntity<ApiResponse<Map<String, Integer>>> deleteAll(
            @RequestParam(defaultValue = "false") boolean confirm
    ) {
        if (!confirm) {
            throw new ApiException(ErrorCode.CONFIRM_REQUIRED);
        }
        int count = store.size();
        store.clear();
        return ResponseEntity.ok(ApiResponse.success(Map.of("deletedCount", count)));
    }

    // 아이템이 없으면 404
    private ItemDto findItemOrThrow(Long id) {
        ItemDto item = store.get(id);
        if (item == null) {
            throw new ApiException(ErrorCode.ITEM_NOT_FOUND);
        }
        return item;
    }
}
