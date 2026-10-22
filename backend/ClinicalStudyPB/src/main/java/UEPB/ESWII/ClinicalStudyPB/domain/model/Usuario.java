package UEPB.ESWII.ClinicalStudyPB.domain.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
public class Usuario {
    
    @Id 
    @GeneratedValue 
    private UUID id;
    private String nome;

}
