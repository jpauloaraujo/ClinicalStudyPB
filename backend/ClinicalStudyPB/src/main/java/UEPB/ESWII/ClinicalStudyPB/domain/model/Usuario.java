package UEPB.ESWII.ClinicalStudyPB.domain.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.PerfilUsuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Schema(description = "Representa um usuário do sistema associado a um dos quatro perfis operacionais")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Identificador único do usuário (UUID)", example = "22222222-2222-4222-8222-222222222222", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
    @Column(nullable = false, length = 120)
    @Schema(description = "Nome completo do usuário (fictício)", example = "Dra. Helena Costa")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Informe um e-mail válido")
    @Size(max = 180)
    @Column(nullable = false, unique = true, length = 180)
    @Schema(description = "Endereço de e-mail único utilizado para autenticação", example = "helena.costa@clinicalstudy.test")
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false, length = 100)
    @Schema(description = "Senha de acesso (mínimo de 8 caracteres; nunca retornada nas respostas)", example = "senha-segura-123", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String senha;

    @NotNull(message = "O perfil é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Schema(description = "Perfil de controle de acesso do usuário", example = "PESQUISADOR")
    private PerfilUsuario perfil;

    @Column(nullable = false)
    @Schema(description = "Indica se a conta do usuário está ativa para acesso ao sistema", example = "true", defaultValue = "true")
    private boolean ativo = true;
}
