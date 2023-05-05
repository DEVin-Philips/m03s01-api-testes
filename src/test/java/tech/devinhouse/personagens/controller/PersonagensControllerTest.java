package tech.devinhouse.personagens.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import tech.devinhouse.personagens.dto.PersonagemRequest;
import tech.devinhouse.personagens.dto.PersonagemResponse;
import tech.devinhouse.personagens.exception.RegistroExistenteException;
import tech.devinhouse.personagens.exception.RegistroNaoEncontradoException;
import tech.devinhouse.personagens.model.Personagem;
import tech.devinhouse.personagens.service.PersonagemService;

import static java.time.Month.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

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

    @Test
    @DisplayName("Quando há personagens registrados, deve retornar lista com personagens")
    void consultar_lista() throws Exception {
        var personagens = List.of(
                new Personagem(1L, 11111111111L, "James Kirk", LocalDate.of(1925, JANUARY, 1), "Star Trek"),
                new Personagem(2L, 22222222222L, "MontGomery Scott", LocalDate.of(1920, APRIL, 3), "Star Trek"),
                new Personagem(3L, 33333333333L, "Spock", LocalDate.of(1900, FEBRUARY, 2), "Star Trek")
        );
        Mockito.when(service.consultar()).thenReturn(personagens);
        mockMvc.perform(get("/api/personagens")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())  // 200
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].cpf", is(personagens.get(0).getCpf())))
                .andExpect(jsonPath("$[0].nome", is(personagens.get(0).getNome())))
                .andExpect(jsonPath("$[0].serie", is(personagens.get(0).getSerie())))
                .andExpect(jsonPath("$[1].cpf", is(personagens.get(1).getCpf())))
                .andExpect(jsonPath("$[1].nome", is(personagens.get(1).getNome())))
                .andExpect(jsonPath("$[1].serie", is(personagens.get(1).getSerie())))
                .andExpect(jsonPath("$[2].cpf", is(personagens.get(2).getCpf())))
                .andExpect(jsonPath("$[2].nome", is(personagens.get(2).getNome())))
                .andExpect(jsonPath("$[2].serie", is(personagens.get(2).getSerie())));
    }

    @Test
    @DisplayName("Quando consulta personagem pelo Id nao cadastrado, deve retornar erro")
    void consultarPorId_naoCadastrado() throws Exception {
        Mockito.when(service.consultar(Mockito.anyLong())).thenThrow(RegistroNaoEncontradoException.class);
        mockMvc.perform(get("/api/personagens/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())  // 404
                .andExpect(jsonPath("$.erro", is(notNullValue())));
    }

    @Test
    @DisplayName("Quando consulta personagem pelo Id cadastrado, deve retornar registro")
    void consultarPorId_cadastrado() throws Exception {
        var personagem = new Personagem(1L, 11111111111L, "James Kirk", LocalDate.of(1925, Month.JANUARY, 1), "Star Trek");
        Mockito.when(service.consultar(Mockito.anyLong())).thenReturn(personagem);
        mockMvc.perform(get("/api/personagens/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf", is(personagem.getCpf())))
                .andExpect(jsonPath("$.nome", is(personagem.getNome())));
    }

    @Test
    @DisplayName("Quando inclusao com dados invalidos, deve retornar erros")
    void incluir_invalido() throws Exception {
        PersonagemRequest req = new PersonagemRequest();
        String requestJson = objectMapper.writeValueAsString(req);
//        System.out.println("Req em json: " + requestJson);
        mockMvc.perform(post("/api/personagens")
                .content(requestJson)  // passar json no request body
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())  // 400
                .andExpect(jsonPath("$.cpf", containsStringIgnoringCase("deve ser informado")));
    }

    @Test
    @DisplayName("Quando inclusao com CPF jah existente, deve retornar erro")
    void incluir_cpfJaCadastrado() throws Exception {
        Mockito.when(service.inserir(Mockito.any(Personagem.class))).thenThrow(RegistroExistenteException.class);
        var req = new PersonagemRequest(11111111111L, "James Kirk", null, "Star Trek");
        String requestJson = objectMapper.writeValueAsString(req);
        mockMvc.perform(post("/api/personagens")
                .content(requestJson)  // request body
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())  // 409
                .andExpect(jsonPath("$.erro", containsStringIgnoringCase("Registro já cadastrado!")));
    }

    @Test
    @DisplayName("Quando inclusao de novo personagem, deve retornar sucesso")
    void incluir_sucesso() throws Exception {
        PersonagemRequest req = new PersonagemRequest(11111111111L, "James Kirk", LocalDate.of(1925, Month.JANUARY, 1), "Star Trek");
        Personagem personagem = modelMapper.map(req, Personagem.class);
        personagem.setId(1L);
        String requestJson = objectMapper.writeValueAsString(req);  // serializa objeto java para json (representada como umaq string)
//        objectMapper.readValue(requestJson, PersonagemRequest.class); // des-serializa um json para objeto Java
        System.out.println(requestJson);
        Mockito.when(service.inserir(Mockito.any(Personagem.class))).thenReturn(personagem);
        mockMvc.perform(post("/api/personagens")
                .content(requestJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())  // 201
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.id", is(personagem.getId().intValue())));
    }

    //TODO: Continuar implementacoes dos demais testes unitarios do controller ...

}