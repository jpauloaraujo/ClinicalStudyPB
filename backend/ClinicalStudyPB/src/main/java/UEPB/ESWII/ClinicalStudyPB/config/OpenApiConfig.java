package UEPB.ESWII.ClinicalStudyPB.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

/**
 * Configuração da documentação interativa da API REST no padrão OpenAPI 3 / Swagger UI (T10).
 *
 * <p>Define metadados da especificação, esquema de autenticação Bearer JWT para testes
 * interativos no Swagger UI e organização por tags funcionais.</p>
 */
@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /** Rotas públicas da documentação OpenAPI / Swagger UI (reutilizável na configuração de segurança T19). */
    public static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**"
    };

    @Bean
    public OpenAPI clinicalStudyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ClinicalStudyPB API")
                        .version("v1.0.0")
                        .description("""
                                API HTTP REST compartilhada pelas interfaces Web (React + TypeScript) e Mobile (React Native + Expo) \
                                do **Sistema de Gestão de Estudos Clínicos (ClinicalStudyPB)** — Engenharia de Software II (UEPB).
                                
                                ### Escopo e Perfis de Acesso
                                - **Administrador (Web):** Cadastro e gerenciamento de usuários e perfis (`RF01`).
                                - **Pesquisador (Web):** Cadastro de protocolos de estudos clínicos (`RF04`), critérios de elegibilidade, visitas previstas e relatórios.
                                - **Coordenador do Estudo (Web):** Recrutamento, avaliação de elegibilidade, consentimento e agendamento de visitas.
                                - **Participante (Mobile):** Consulta de estudos vinculados, confirmação de visitas e solicitações.
                                
                                > **Nota:** Este sistema tem finalidade exclusivamente acadêmica e opera apenas com **dados fictícios**.
                                """)
                        .contact(new Contact()
                                .name("Equipe ClinicalStudyPB — UEPB (Engenharia de Software II)")
                                .url("https://github.com/jpauloaraujo/ClinicalStudyPB"))
                        .license(new License()
                                .name("Uso Acadêmico — UEPB")
                                .url("https://github.com/jpauloaraujo/ClinicalStudyPB")))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentação de Arquitetura e Especificação (Repositório GitHub)")
                        .url("https://github.com/jpauloaraujo/ClinicalStudyPB/tree/main/docs"))
                .servers(List.of(
                        new Server()
                                .url("/")
                                .description("Servidor Atual (Relativo)"),
                        new Server()
                                .url("http://localhost:8081")
                                .description("Ambiente de Desenvolvimento Local (Porta 8081)")))
                .tags(List.of(
                        new Tag()
                                .name("Usuários")
                                .description("RF01 — Cadastro, consulta, atualização e exclusão de usuários por perfil"),
                        new Tag()
                                .name("Estudos Clínicos")
                                .description("RF04 — Cadastro, consulta, atualização e exclusão de protocolos de estudos clínicos")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Informe o token JWT obtido no endpoint de login (sem o prefixo 'Bearer ').")));
    }
}
