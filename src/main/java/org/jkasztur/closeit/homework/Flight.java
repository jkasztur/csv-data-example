package org.jkasztur.closeit.homework;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "Flight")
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Flight  implements Serializable {

	// only needed properties are specified

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private String id;
	@JsonProperty("Dest")
	private String dest;
	@JsonProperty("Cancelled")
	private int cancelled;
	@JsonProperty("ArrDelay")
	private String arrDelay;

}
