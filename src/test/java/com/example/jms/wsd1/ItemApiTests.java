package com.example.jms.wsd1;

import com.example.jms.wsd1.admin.MaintenanceState;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** v3(과제 버전) API와 2xx/4xx/5xx 응답, 표준 응답 형식을 검증한다. */
@SpringBootTest
@AutoConfigureMockMvc
class ItemApiTests {

    private static final String V3 = "/api/v3/items";

    @Autowired MockMvc mvc;
    @Autowired MaintenanceState maintenanceState;

    @BeforeEach
    void clear() throws Exception {
        maintenanceState.setEnabled(false);
        mvc.perform(delete(V3).param("confirm", "true"));
    }

    @AfterEach
    void reset() {
        maintenanceState.setEnabled(false);
    }

    private ResultActions postJson(String url, String body) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions putJson(String url, String body) throws Exception {
        return mvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private int createItem(String name, int price) throws Exception {
        String json = postJson(V3, "{\"name\":\"" + name + "\",\"price\":" + price + "}")
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.data.id");
    }

    // ───── POST ─────

    @Test
    void 단건등록_201() throws Exception {
        postJson(V3, "{\"name\":\"item-1\",\"price\":30000}")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith(V3 + "/")))
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.name").value("item-1"))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void 헤더등록_201_헤더값반환() throws Exception {
        mvc.perform(post(V3 + "/with-header").contentType(MediaType.APPLICATION_JSON)
                        .header("X-USER-ID", "user1")
                        .header("Authorization", "Bearer test-token")
                        .content("{\"name\":\"item-2\",\"price\":15000}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.item.name").value("item-2"))
                .andExpect(jsonPath("$.data.headers['X-USER-ID']").value("user1"))
                .andExpect(jsonPath("$.data.headers.Authorization").value("Bearer test-token"));
    }

    @Test
    void 헤더없이등록해도_201() throws Exception {
        postJson(V3 + "/with-header", "{\"name\":\"item-2\",\"price\":15000}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.item.name").value("item-2"));
    }

    // ───── GET ─────

    @Test
    void 전체조회_200() throws Exception {
        createItem("item-1", 30000);
        createItem("item-2", 15000);
        mvc.perform(get(V3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void 단건조회_200() throws Exception {
        int id = createItem("item-1", 30000);
        mvc.perform(get(V3 + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    void 검색_200() throws Exception {
        createItem("item-1", 30000);
        createItem("edited-item-1", 80000);
        createItem("item-2", 15000);
        mvc.perform(get(V3 + "/search").param("keyword", "item-1"))   // item-1, edited-item-1
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    // ───── PUT ─────

    @Test
    void 수정_보낸필드만변경_200() throws Exception {
        int id = createItem("item-1", 30000);
        putJson(V3 + "/" + id, "{\"name\":\"edited-item-1\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("edited-item-1"))
                .andExpect(jsonPath("$.data.price").value(30000));
    }

    @Test
    void 가격수정_200() throws Exception {
        int id = createItem("item-1", 30000);
        putJson(V3 + "/" + id + "/price", "{\"price\":25000}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.price").value(25000));
    }

    // ───── DELETE ─────

    @Test
    void 단건삭제_200_후_조회_404() throws Exception {
        int id = createItem("item-1", 30000);
        mvc.perform(delete(V3 + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id));
        mvc.perform(get(V3 + "/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void 전체삭제_200() throws Exception {
        createItem("a", 1);
        createItem("b", 2);
        mvc.perform(delete(V3).param("confirm", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deletedCount").value(2));
    }

    // ───── 4xx ─────

    @Test
    void 입력검증실패_400() throws Exception {
        postJson(V3, "{\"name\":\"\",\"price\":-1}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void 빈수정요청_400() throws Exception {
        int id = createItem("item-1", 30000);
        putJson(V3 + "/" + id, "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EMPTY_UPDATE"));
    }

    @Test
    void 깨진JSON_400() throws Exception {
        postJson(V3, "{\"name\":")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_JSON"));
    }

    @Test
    void 잘못된ID형식_400() throws Exception {
        mvc.perform(get(V3 + "/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    @Test
    void 잘못된페이지크기_400() throws Exception {
        mvc.perform(get(V3 + "/search").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void 전체삭제_confirm없음_400() throws Exception {
        mvc.perform(delete(V3))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CONFIRM_REQUIRED"));
    }

    @Test
    void 없는아이템_404() throws Exception {
        mvc.perform(get(V3 + "/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ITEM_NOT_FOUND"));
        putJson(V3 + "/999/price", "{\"price\":1}").andExpect(status().isNotFound());
        mvc.perform(delete(V3 + "/999")).andExpect(status().isNotFound());
    }

    @Test
    void 없는경로_404() throws Exception {
        mvc.perform(get("/api/nothing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("API_NOT_FOUND"));
    }

    @Test
    void 지원하지않는메소드_405() throws Exception {
        mvc.perform(patch(V3 + "/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    // ───── 5xx ─────

    @Test
    void 내부오류_500_세부내용숨김() throws Exception {
        mvc.perform(get("/admin/error-test"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("서버 처리 중 오류가 발생했습니다."));
    }

    @Test
    void 점검모드_503_해제후_200() throws Exception {
        putJson("/admin/maintenance", "{\"enabled\":true}").andExpect(status().isOk());
        mvc.perform(get(V3))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.code").value("SERVICE_UNAVAILABLE"));
        putJson("/admin/maintenance", "{\"enabled\":false}").andExpect(status().isOk());
        mvc.perform(get(V3)).andExpect(status().isOk());
    }

    // ───── 수업 예제 v1이 원본 동작 그대로인지 확인 ─────

    @Test
    void 수업예제_v1_원본동작유지() throws Exception {
        postJson("/api/v1/items", "{\"name\":\"연필\",\"price\":500}")
                .andExpect(status().isOk())                       // 원본은 200
                .andExpect(jsonPath("$.name").value("연필"))       // 표준 형식으로 감싸지 않음
                .andExpect(jsonPath("$.status").doesNotExist());
        mvc.perform(get("/api/v1/items/999")).andExpect(status().isNotFound());
    }
}
