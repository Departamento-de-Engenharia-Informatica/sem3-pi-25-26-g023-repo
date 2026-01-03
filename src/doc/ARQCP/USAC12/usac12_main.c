#include <stdio.h>
#include <string.h>
#include "usac12.h"

void display_menu() {
    printf("\n=== USAC12 - User Action Log Generator ===\n\n");
    printf("1. Generate admin log file\n");
    printf("2. Generate operator log file\n");
    printf("3. Generate custom user log\n");
    printf("4. Display sample actions\n");
    printf("5. Save actions to custom file\n");
    printf("0. Exit\n");
    printf("\nChoose option: ");
}

int main() {
    int choice;
    char username[USERNAME_LEN];
    char filename[FILENAME_LEN];
    ActionLog log;

    printf("USAC12 - Create text file with user actions sequence\n");
    printf("====================================================\n\n");

    do {
        display_menu();
        scanf("%d", &choice);
        getchar(); // Limpa newline

        switch (choice) {
            case 1:
                if (create_user_log_file("admin", "admin_actions.txt")) {
                    printf("✓ Admin log created: admin_actions.txt\n");
                } else {
                    printf("✗ Failed to create log\n");
                }
                break;

            case 2:
                if (create_user_log_file("operator", "operator_actions.txt")) {
                    printf("✓ Operator log created: operator_actions.txt\n");
                } else {
                    printf("✗ Failed to create log\n");
                }
                break;

            case 3:
                printf("Enter username: ");
                fgets(username, USERNAME_LEN, stdin);
                username[strcspn(username, "\n")] = '\0';

                printf("Enter filename: ");
                fgets(filename, FILENAME_LEN, stdin);
                filename[strcspn(filename, "\n")] = '\0';

                if (create_user_log_file(username, filename)) {
                    printf("✓ Log created: %s\n", filename);
                }
                break;

            case 4:
                printf("\nSample actions for 'operator':\n");
                read_user_actions("operator", &log);

                for (int i = 0; i < log.count; i++) {
                    printf("%d. %s\n", i+1, log.actions[i].action);
                }
                break;

            case 5:
                printf("Enter username to save actions: ");
                fgets(username, USERNAME_LEN, stdin);
                username[strcspn(username, "\n")] = '\0';

                printf("Enter output filename: ");
                fgets(filename, FILENAME_LEN, stdin);
                filename[strcspn(filename, "\n")] = '\0';

                read_user_actions(username, &log);
                if (save_actions_to_file(&log, filename)) {
                    printf("✓ Actions saved to: %s\n", filename);
                }
                break;

            case 0:
                printf("Exiting...\n");
                break;

            default:
                printf("Invalid option!\n");
        }

        printf("\n");

    } while (choice != 0);

    return 0;
}