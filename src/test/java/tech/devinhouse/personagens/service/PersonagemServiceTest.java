package tech.devinhouse.personagens.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.devinhouse.personagens.exception.RegistroNaoEncontradoException;
import tech.devinhouse.personagens.model.Personagem;
import tech.devinhouse.personagens.repository.PersonagemRepository;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class) // configurando classe de teste para rodar com o Mockito
class PersonagemServiceTest {

    @Mock   // criar objeto mock da dependencia da classe testada
    private PersonagemRepository personagemRepo;

    @InjectMocks  // injeta os mocks criados na classe sendo testada
    private PersonagemService service;

    @Test
    @DisplayName("Quando existe personagem com o id informado, deve retornar o personagem")
    void consultar() {
        // given
        Long id = 1L;
        Personagem personagem = Personagem.builder().id(id).nome("Nome").cpf(12345678901L).build();
        Mockito.when(personagemRepo.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(personagem));
        // when
        Personagem resultado = service.consultar(id);
        // then
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
    }

    @Test
    @DisplayName("Quando nao existe personagem com o id informado, deve lancar excecao")
    void consultar_naoEncontrado() {
        Long id = 1L;
        // Nao eh necessario a instrucao abaixo pq o comportamento padrao do Mock eh nao retornar nada
//        Mockito.when(personagemRepo.findById(Mockito.anyLong()))
//                .thenReturn(Optional.empty());
        assertThrows(RegistroNaoEncontradoException.class, () -> service.consultar(id));
    }

}