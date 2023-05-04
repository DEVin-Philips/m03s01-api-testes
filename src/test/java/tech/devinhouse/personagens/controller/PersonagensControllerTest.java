package tech.devinhouse.personagens.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import tech.devinhouse.personagens.service.PersonagemService;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)  // configurando classe de teste para rodar com o Spring (Spring Beans, http, etc...)
@WebMvcTest  // para termos acesso à objetos de chamadas http
class PersonagensControllerTest {

    @Autowired
    private MockMvc mockMvc;  // objeto para fazer requisicoes http

    @Autowired
    private ObjectMapper objectMapper; // classe que serializa Objetos para JSON

    @Autowired
    private ModelMapper modelMapper;  // classe que transforma classes entre si, copiando os atributos

    @MockBean  // mock para dependencias da classe de controller
    private PersonagemService service;


    @Test
    @DisplayName("Quando nao há personagens registrados, deve retornar lista vazia")
    void consultar_vazio() throws Exception {
        mockMvc.perform(get("/api/personagens")
                .contentType(MediaType.APPLICATION_JSON)) // response body com JSON
                .andExpect(status().isOk())  // 200
                .andExpect(jsonPath("$", is(empty())));
    }

}