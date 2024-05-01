package edu.ucsb.cs156.example.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "UCSBORGANIZATIONS")
public class UCSBOrganization {
  @Id
  private String orgCode;
  private String orgTranslationShort;
  private String orgTranslation;
  private boolean inactive;
}