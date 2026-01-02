#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <dirent.h>
#include <sys/stat.h>
#include "config_loader.h"
#include "../common/colors.h"
#include "../common/utils.h"

// ============================================
// USAC11 MAIN - Sistema de Configuração Inicial
// ============================================

// Função para exibir menu principal
void display_main_menu() {
    printf("\n%s==========================================%s\n", COLOR_CYAN, COLOR_RESET);
    printf("%s     USAC11 - SISTEMA DE CONFIGURAÇÃO     %s\n", COLOR_CYAN, COLOR_RESET);
    printf("%s==========================================%s\n\n", COLOR_CYAN, COLOR_RESET);

    const char* options[] = {
        "Carregar arquivo de configuração",
        "Carregar configuração do diretório",
        "Validar arquivo de configuração",
        "Exibir configuração atual",
        "Exibir estatísticas do sistema",
        "Salvar configuração atual",
        "Criar backup da configuração",
        "Exportar configuração para CSV",
        "Verificar consistência",
        "Executar testes de configuração"
    };

    display_menu("MENU PRINCIPAL", options, 10);
}

// Função para carregar arquivo específico
void load_config_file_menu() {
    char filename[100];

    printf("\n%s=== CARREGAR ARQUIVO DE CONFIGURAÇÃO ===%s\n",
           COLOR_BOLD COLOR_BLUE, COLOR_RESET);

    printf("\nArquivos disponíveis em config_files/:\n");
    printf("%s", COLOR_CYAN);

    // Listar arquivos no diretório
    DIR* dir = opendir("config_files");
    if (dir != NULL) {
        struct dirent* entry;
        int count = 0;
        while ((entry = readdir(dir)) != NULL) {
            if (strstr(entry->d_name, ".txt") != NULL) {
                printf("  • %s\n", entry->d_name);
                count++;
            }
        }
        closedir(dir);

        if (count == 0) {
            printf("  (nenhum arquivo encontrado)\n");
        }
    } else {
        printf("  (diretório config_files/ não encontrado)\n");
    }

    printf("%s\n", COLOR_RESET);

    printf("Digite o nome do arquivo (ou 'padrao' para station_config.txt): ");
    if (!get_valid_input("%99s", filename)) {
        PRINT_ERROR("Entrada inválida!");
        return;
    }

    if (strcmp(filename, "padrao") == 0) {
        strcpy(filename, "config_files/station_config.txt");
    } else if (!strstr(filename, "config_files/")) {
        char full_path[150];
        snprintf(full_path, sizeof(full_path), "config_files/%s", filename);
        strcpy(filename, full_path);
    }

    printf("\n%sCarregando: %s%s\n", COLOR_INFO, filename, COLOR_RESET);

    if (load_initial_config(filename)) {
        PRINT_SUCCESS("Configuração carregada com sucesso!");
    } else {
        PRINT_ERROR("Falha ao carregar configuração!");
    }
}

// Função para carregar do diretório
void load_directory_menu() {
    char dirname[100];

    printf("\n%s=== CARREGAR DO DIRETÓRIO ===%s\n",
           COLOR_BOLD COLOR_BLUE, COLOR_RESET);

    printf("Diretório atual: ");
    system("pwd");

    printf("\nDiretório padrão: config_files/\n");
    printf("Digite o diretório (ou Enter para padrão): ");

    fgets(dirname, sizeof(dirname), stdin);
    dirname[strcspn(dirname, "\n")] = '\0';

    if (strlen(dirname) == 0) {
        strcpy(dirname, "config_files");
    }

    load_config_from_directory(dirname);
}

// Função para validar arquivo
void validate_file_menu() {
    char filename[100];

    printf("\n%s=== VALIDAR ARQUIVO DE CONFIGURAÇÃO ===%s\n",
           COLOR_BOLD COLOR_BLUE, COLOR_RESET);

    printf("Digite o nome do arquivo para validar: ");
    if (!get_valid_input("%99s", filename)) {
        PRINT_ERROR("Entrada inválida!");
        return;
    }

    // Adicionar caminho se não tiver
    if (!strstr(filename, "config_files/") && access(filename, F_OK) != 0) {
        char full_path[150];
        snprintf(full_path, sizeof(full_path), "config_files/%s", filename);
        strcpy(filename, full_path);
    }

    ConfigValidation validation = validate_config_file(filename);
    print_config_validation(&validation);
}

// Função para salvar configuração atual
void save_config_menu() {
    char filename[100];

    printf("\n%s=== SALVAR CONFIGURAÇÃO ATUAL ===%s\n",
           COLOR_BOLD COLOR_BLUE, COLOR_RESET);

    time_t now = time(NULL);
    struct tm* tm_info = localtime(&now);
    char default_name[50];
    strftime(default_name, sizeof(default_name),
             "config_save_%Y%m%d_%H%M%S.txt", tm_info);

    printf("Nome padrão: %s\n", default_name);
    printf("Digite o nome do arquivo (ou Enter para padrão): ");

    fgets(filename, sizeof(filename), stdin);
    filename[strcspn(filename, "\n")] = '\0';

    if (strlen(filename) == 0) {
        strcpy(filename, default_name);
    }

    // Adicionar caminho se não tiver
    if (!strstr(filename, "config_files/") && filename[0] != '/') {
        char full_path[150];
        snprintf(full_path, sizeof(full_path), "config_files/%s", filename);
        strcpy(filename, full_path);
    }

    if (save_current_config(filename)) {
        PRINT_SUCCESS("Configuração salva com sucesso!");
    } else {
        PRINT_ERROR("Falha ao salvar configuração!");
    }
}

// Função para backup
void backup_menu() {
    char backup_name[100];

    printf("\n%s=== CRIAR BACKUP DA CONFIGURAÇÃO ===%s\n",
           COLOR_BOLD COLOR_BLUE, COLOR_RESET);

    printf("Digite um nome para o backup (ou Enter para nome automático): ");

    fgets(backup_name, sizeof(backup_name), stdin);
    backup_name[strcspn(backup_name, "\n")] = '\0';

    if (strlen(backup_name) == 0) {
        if (create_config_backup(NULL)) {
            PRINT_SUCCESS("Backup criado com sucesso!");
        } else {
            PRINT_ERROR("Falha ao criar backup!");
        }
    } else {
        if (create_config_backup(backup_name)) {
            PRINT_SUCCESS("Backup criado com sucesso!");
        } else {
            PRINT_ERROR("Falha ao criar backup!");
        }
    }
}

// Função para exportar CSV
void export_csv_menu() {
    char filename[100];

    printf("\n%s=== EXPORTAR PARA CSV ===%s\n",
           COLOR_BOLD COLOR_BLUE, COLOR_RESET);

    time_t now = time(NULL);
    struct tm* tm_info = localtime(&now);
    char default_name[50];
    strftime(default_name, sizeof(default_name),
             "config_export_%Y%m%d.csv", tm_info);

    printf("Nome padrão: %s\n", default_name);
    printf("Digite o nome do arquivo (ou Enter para padrão): ");

    fgets(filename, sizeof(filename), stdin);
    filename[strcspn(filename, "\n")] = '\0';

    if (strlen(filename) == 0) {
        strcpy(filename, default_name);
    }

    if (export_config_to_csv(filename)) {
        printf("%sConfiguração exportada para: %s%s\n",
               COLOR_GREEN, filename, COLOR_RESET);
        printf("%sUse: column -s, -t < %s | less -S%s\n",
               COLOR_CYAN, filename, COLOR_RESET);
    } else {
        PRINT_ERROR("Falha ao exportar configuração!");
    }
}

// Função para executar testes
void run_config_tests() {
    printf("\n%s=== EXECUTANDO TESTES DE CONFIGURAÇÃO ===%s\n",
           COLOR_BOLD COLOR_BLUE, COLOR_RESET);

    // Teste 1: Validar arquivo de exemplo
    printf("\n1. Validando arquivo de exemplo...\n");
    ConfigValidation sample_validation = validate_config_file("config_files/sample_config.txt");
    print_config_validation(&sample_validation);

    // Teste 2: Carregar configuração mínima
    printf("\n2. Carregando configuração mínima...\n");
    if (load_initial_config("config_files/sample_config.txt")) {
        PRINT_SUCCESS("Configuração mínima carregada!");
    } else {
        PRINT_ERROR("Falha ao carregar configuração mínima!");
    }

    // Teste 3: Verificar consistência
    printf("\n3. Verificando consistência...\n");
    if (verify_config_consistency()) {
        PRINT_SUCCESS("Configuração consistente!");
    } else {
        PRINT_WARNING("Problemas de consistência encontrados!");
    }

    // Teste 4: Exibir configuração
    printf("\n4. Exibindo configuração atual...\n");
    print_current_config();

    // Teste 5: Salvar e recarregar
    printf("\n5. Testando save/load...\n");
    if (save_current_config("config_files/test_save.txt")) {
        printf("   Configuração salva, tentando recarregar...\n");
        // Reset counts for test
        user_count = 0;
        track_count = 0;

        if (load_initial_config("config_files/test_save.txt")) {
            PRINT_SUCCESS("Save/load test PASSED!");
        } else {
            PRINT_ERROR("Save/load test FAILED!");
        }

        // Cleanup
        remove("config_files/test_save.txt");
    }

    printf("\n%s=== TESTES CONCLUÍDOS ===%s\n",
           COLOR_BOLD COLOR_GREEN, COLOR_RESET);
}

// Função principal
int main() {
    printf("%s==========================================%s\n", COLOR_BOLD COLOR_CYAN, COLOR_RESET);
    printf("%s   USAC11 - CONFIGURAÇÃO INICIAL DO SISTEMA   %s\n", COLOR_BOLD COLOR_CYAN, COLOR_RESET);
    printf("%s==========================================%s\n\n", COLOR_BOLD COLOR_CYAN, COLOR_RESET);

    PRINT_INFO("Inicializando sistema de configuração...");

    // Criar diretório de configuração se não existir
    struct stat st = {0};
    if (stat("config_files", &st) == -1) {
        mkdir("config_files", 0700);
        PRINT_INFO("Diretório config_files/ criado");
    }

    // Carregar configuração padrão inicialmente
    PRINT_INFO("Carregando configuração padrão...");
    load_initial_config("config_files/station_config.txt");

    int choice;
    do {
        display_main_menu();
        printf("%sEscolha uma opção (0 para sair): %s", COLOR_GREEN, COLOR_RESET);

        if (!get_valid_input("%d", &choice)) {
            PRINT_ERROR("Entrada inválida!");
            clear_input_buffer();
            continue;
        }

        switch (choice) {
            case 1:
                load_config_file_menu();
                break;
            case 2:
                load_directory_menu();
                break;
            case 3:
                validate_file_menu();
                break;
            case 4:
                print_current_config();
                break;
            case 5:
                print_config_statistics();
                break;
            case 6:
                save_config_menu();
                break;
            case 7:
                backup_menu();
                break;
            case 8:
                export_csv_menu();
                break;
            case 9:
                verify_config_consistency();
                break;
            case 10:
                run_config_tests();
                break;
            case 0:
                PRINT_SUCCESS("Saindo do sistema de configuração...");
                break;
            default:
                PRINT_ERROR("Opção inválida!");
        }

        if (choice != 0) {
            printf("\n%sPressione Enter para continuar...%s", COLOR_YELLOW, COLOR_RESET);
            getchar();
            clear_input_buffer();
        }

    } while (choice != 0);

    // Salvar configuração final
    printf("\n%sSalvando configuração final...%s\n", COLOR_INFO, COLOR_RESET);
    save_current_config("config_files/final_config.txt");

    printf("\n%s==========================================%s\n", COLOR_BOLD COLOR_GREEN, COLOR_RESET);
    printf("%s       CONFIGURAÇÃO CONCLUÍDA           %s\n", COLOR_BOLD COLOR_GREEN, COLOR_RESET);
    printf("%s==========================================%s\n", COLOR_BOLD COLOR_GREEN, COLOR_RESET);

    printf("\n%sResumo final:%s\n", COLOR_BOLD COLOR_CYAN, COLOR_RESET);
    printf("  • Usuários configurados: %d\n", user_count);
    printf("  • Trilhos configurados: %d\n", track_count);
    printf("  • Arquivos carregados: %d\n", config_files_count);

    if (last_config_load_time > 0) {
        char time_str[30];
        format_timestamp(last_config_load_time, time_str, sizeof(time_str));
        printf("  • Última carga: %s\n", time_str);
    }

    printf("\n%sConfiguração salva em: config_files/final_config.txt%s\n",
           COLOR_GREEN, COLOR_RESET);

    return 0;
}