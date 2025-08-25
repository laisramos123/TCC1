package com.example.auth_server.enums;

public enum Permission {
    // Accounts
    ACCOUNTS_READ("Dados da conta"),
    ACCOUNTS_BALANCES_READ("Saldos da conta"),
    ACCOUNTS_TRANSACTIONS_READ("Transações da conta"),
    ACCOUNTS_OVERDRAFT_LIMITS_READ("Limites da conta"),

    // Credit Cards
    CREDIT_CARDS_ACCOUNTS_READ("Dados do cartão de crédito"),
    CREDIT_CARDS_ACCOUNTS_BILLS_READ("Faturas do cartão de crédito"),
    CREDIT_CARDS_ACCOUNTS_BILLS_TRANSACTIONS_READ("Transações das faturas"),
    CREDIT_CARDS_ACCOUNTS_LIMITS_READ("Limites do cartão de crédito"),
    CREDIT_CARDS_ACCOUNTS_TRANSACTIONS_READ("Transações do cartão de crédito"),

    // Customers
    CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ("Identificação de pessoa física"),
    CUSTOMERS_PERSONAL_ADITTIONALINFO_READ("Informações complementares PF"),
    CUSTOMERS_BUSINESS_IDENTIFICATIONS_READ("Identificação de pessoa jurídica"),
    CUSTOMERS_BUSINESS_ADITTIONALINFO_READ("Informações complementares PJ"),

    // Financings
    FINANCINGS_READ("Dados do financiamento"),
    FINANCINGS_SCHEDULED_INSTALMENTS_READ("Parcelas do financiamento"),
    FINANCINGS_PAYMENTS_READ("Pagamentos do financiamento"),
    FINANCINGS_WARRANTIES_READ("Garantias do financiamento"),

    // Invoice Financings
    INVOICE_FINANCINGS_READ("Dados da antecipação de recebíveis"),
    INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ("Parcelas da antecipação"),
    INVOICE_FINANCINGS_PAYMENTS_READ("Pagamentos da antecipação"),
    INVOICE_FINANCINGS_WARRANTIES_READ("Garantias da antecipação"),

    // Loans
    LOANS_READ("Dados do empréstimo"),
    LOANS_SCHEDULED_INSTALMENTS_READ("Parcelas do empréstimo"),
    LOANS_PAYMENTS_READ("Pagamentos do empréstimo"),
    LOANS_WARRANTIES_READ("Garantias do empréstimo"),

    // Unarranged Accounts Overdraft
    UNARRANGED_ACCOUNTS_OVERDRAFT_READ("Dados do cheque especial"),
    UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ("Parcelas do cheque especial"),
    UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ("Pagamentos do cheque especial"),
    UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ("Garantias do cheque especial"),

    // Resources
    RESOURCES_READ("Leitura de recursos");

    private final String description;

    Permission(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
