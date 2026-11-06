package com.aula.fixa.domain.model;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Filtros comuns enviados pelas telas de listagem às consultas remotas. */
public final class FiltroBusca {
    public final String termo;
    public final LocalDateTime dataInicio;
    public final LocalDateTime dataFim;
    public final String status;
    public final String local;
    public final String categoriaEquipamento;
    public final String categoriaProblema;
    public final String prioridade;
    public final String autor;
    public final LocalDateTime prazoInicio;
    public final LocalDateTime prazoFim;

    public FiltroBusca(String termo, LocalDateTime dataInicio, LocalDateTime dataFim, String status,
            String local, String categoriaEquipamento, String categoriaProblema, String prioridade,
            String autor) {
        this.termo = clean(termo);
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = clean(status);
        this.local = clean(local);
        this.categoriaEquipamento = clean(categoriaEquipamento);
        this.categoriaProblema = clean(categoriaProblema);
        this.prioridade = clean(prioridade);
        this.autor = clean(autor);
        this.prazoInicio = null;
        this.prazoFim = null;
    }

    private FiltroBusca(String termo, LocalDateTime dataInicio, LocalDateTime dataFim, String status,
            String local, String categoriaEquipamento, String categoriaProblema, String prioridade,
            String autor, LocalDateTime prazoInicio, LocalDateTime prazoFim) {
        this.termo = clean(termo);
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = clean(status);
        this.local = clean(local);
        this.categoriaEquipamento = clean(categoriaEquipamento);
        this.categoriaProblema = clean(categoriaProblema);
        this.prioridade = clean(prioridade);
        this.autor = clean(autor);
        this.prazoInicio = prazoInicio;
        this.prazoFim = prazoFim;
    }

    public static FiltroBusca ultimosTresMeses() {
        return periodo(null, null, null, null, null, null, null, null);
    }

    public static FiltroBusca periodo(String periodo, String termo, String status, String local,
            String categoriaEquipamento, String categoriaProblema, String prioridade, String autor) {
        LocalDateTime now = LocalDateTime.now();
        String normalized = periodo == null ? "" : periodo.trim().toLowerCase(Locale.ROOT);
        LocalDateTime inicio;
        LocalDateTime fim = now;
        if (normalized.startsWith("todos")) {
            inicio = null;
            fim = null;
        } else if (normalized.contains("30")) {
            inicio = now.minusDays(30);
        } else if (normalized.contains("12") || normalized.contains("ano")) {
            inicio = now.minusYears(1);
        } else {
            // Período vazio usa o padrão comum de três meses.
            inicio = now.minusMonths(3);
        }
        return new FiltroBusca(termo, inicio, fim, status, local, categoriaEquipamento,
                categoriaProblema, prioridade, autor);
    }

    public String dataInicioIso() { return iso(dataInicio); }
    public String dataFimIso() { return iso(dataFim); }
    public String prazoInicioIso() { return iso(prazoInicio); }
    public String prazoFimIso() { return iso(prazoFim); }

    public static FiltroBusca quadroOS(String periodo, String termo, String status,
            String categoriaEquipamento, String categoriaProblema, String prioridade, String autor) {
        LocalDateTime now = LocalDateTime.now();
        String selected = periodo == null ? "" : periodo.trim().toLowerCase(Locale.ROOT);
        LocalDateTime dueStart = null;
        LocalDateTime dueEnd = null;
        if (!selected.startsWith("todos")) {
            dueEnd = now;
            dueStart = selected.contains("30") ? now.minusDays(30)
                    : selected.contains("12") || selected.contains("ano") ? now.minusYears(1)
                    : now.minusMonths(3);
        }
        // O intervalo padrão e os períodos escolhidos referem-se ao prazo de entrega,
        // não à data de criação da ordem de serviço.
        return new FiltroBusca(termo, null, null, status, null, categoriaEquipamento,
                categoriaProblema, prioridade, autor, dueStart, dueEnd);
    }

    public static FiltroBusca eventosNaData(LocalDate data) {
        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.plusDays(1).atStartOfDay().minusNanos(1);
        return new FiltroBusca(null, null, null, null, null, null, null, null,
                null, inicio, fim);
    }

    private static String iso(LocalDateTime value) {
        return value == null ? null : value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private static String clean(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        return cleaned.isEmpty() || cleaned.equalsIgnoreCase("Todos") || cleaned.equalsIgnoreCase("Todas")
                || cleaned.startsWith("Selecione") ? null : cleaned;
    }
}
