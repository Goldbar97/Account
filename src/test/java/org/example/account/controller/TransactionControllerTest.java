package org.example.account.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.account.dto.AccountDto;
import org.example.account.dto.CancelBalance;
import org.example.account.dto.TransactionDto;
import org.example.account.dto.UseBalance;
import org.example.account.service.TransactionService;
import org.example.account.type.TransactionResultType;
import org.example.account.type.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {
    @MockBean
    private TransactionService transactionService;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void successUseBalance() throws Exception {
        // given
        given(transactionService.useBalance(anyLong(), anyString(), anyLong()))
                .willReturn(TransactionDto.builder()
                                    .accountNumber("1000000000")
                                    .transactedAt(LocalDateTime.now())
                                    .amount(12345L)
                                    .transactionId("transactionId")
                                    .transactionResultType(
                                            TransactionResultType.S)
                                    .build());
        
        // when
        // then
        mockMvc.perform(post("/transaction/use")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UseBalance.Request(1L, "2000000000",
                                                               3000L
                                        ))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1000000000"))
                .andExpect(jsonPath("$.transactionResult").value("S"))
                .andExpect(jsonPath("$.transactionId").value("transactionId"))
                .andExpect(jsonPath("$.amount").value(12345));
    }
    
    @Test
    void successCancelBalance() throws Exception {
        // given
        given(transactionService.cancelBalance(anyString(), anyString(), anyLong()))
                .willReturn(TransactionDto.builder()
                                    .accountNumber("1000000000")
                                    .transactedAt(LocalDateTime.now())
                                    .amount(54321L)
                                    .transactionId("transactionIdForCancel")
                                    .transactionResultType(
                                            TransactionResultType.S)
                                    .build());
        
        // when
        // then
        mockMvc.perform(post("/transaction/cancel")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new CancelBalance.Request(
                                                "transactionId", "1000000000",
                                                                  3000L
                                        ))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1000000000"))
                .andExpect(jsonPath("$.transactionResult").value("S"))
                .andExpect(jsonPath("$.transactionId").value(
                        "transactionIdForCancel"))
                .andExpect(jsonPath("$.amount").value(54321L));
    }
    
    @Test
    void successQueryTransaction() throws Exception {
        // given
        List<AccountDto> accountDtos = Arrays.asList(
                AccountDto.builder()
                        .accountNumber(
                                "0000000000")
                        .balance(1000L)
                        .build(),
                AccountDto.builder()
                        .accountNumber(
                                "1111111111")
                        .balance(2000L)
                        .build(),
                AccountDto.builder()
                        .accountNumber(
                                "2222222222")
                        .balance(3000L)
                        .build()
        );
        
        given(transactionService.queryTransaction(anyString()))
                .willReturn(TransactionDto.builder()
                                    .transactionType(TransactionType.USE)
                                    .accountNumber("1000000000")
                                    .transactedAt(LocalDateTime.now())
                                    .amount(54321L)
                                    .transactionId("transactionIdForCancel")
                                    .transactionResultType(
                                            TransactionResultType.S)
                                    .build());
        
        // when
        // then
        mockMvc.perform(get("/transaction/12345"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1000000000"))
                .andExpect(jsonPath("$.transactionType").value("USE"))
                .andExpect(jsonPath("$.transactionResult").value("S"))
                .andExpect(jsonPath("$.transactionId").value(
                        "transactionIdForCancel"))
                .andExpect(jsonPath("$.amount").value(54321L));
    }
}