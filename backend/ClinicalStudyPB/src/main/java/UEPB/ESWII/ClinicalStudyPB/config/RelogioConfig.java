package UEPB.ESWII.ClinicalStudyPB.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Relógio único da aplicação (Sprint 3): todo "agora" e "hoje" dos serviços vem deste {@link Clock}, no fuso do centro
 * de pesquisa ({@code app.fuso-horario}), para os testes poderem fixar o dia e a hora.
 */
@Configuration
public class RelogioConfig {

    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock clock(@Value("${app.fuso-horario:America/Recife}") String fusoHorario) {
        return Clock.system(ZoneId.of(fusoHorario));
    }
}
