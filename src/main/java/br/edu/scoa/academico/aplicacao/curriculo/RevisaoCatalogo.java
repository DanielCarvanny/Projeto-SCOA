package br.edu.scoa.academico.aplicacao.curriculo;
import java.time.Instant;
public record RevisaoCatalogo(ItemCatalogo item, long autorId, Instant instante, String operacaoId) {}

