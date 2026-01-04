#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include "manager_setup.h"

/* ---------- helpers internos ---------- */

static void manager_init_empty(ManagerData *data) {
    data->users = NULL;
    data->num_users = 0;

    data->tracks = NULL;
    data->num_tracks = 0;

    data->trains = NULL;
    data->num_trains = 0;

    data->logs = NULL;
    data->num_logs = 0;
    data->logs_capacity = 0;
    data->next_log_id = 1;

    data->sensors.temp.buffer_length = 0;
    data->sensors.temp.mm_window     = 0;
    data->sensors.hum.buffer_length  = 0;
    data->sensors.hum.mm_window      = 0;
}

void manager_free(ManagerData *data) {
    if (!data) return;

    free(data->users);
    free(data->tracks);
    free(data->trains);
    free(data->logs);

    manager_init_empty(data);
}

static void trim(char *s) {
    if (!s) return;
    int start = 0;
    while (isspace((unsigned char)s[start])) start++;

    int end = (int)strlen(s) - 1;
    while (end >= start && isspace((unsigned char)s[end])) {
        s[end] = '\0';
        end--;
    }

    if (start > 0) {
        int i = 0;
        while (s[start + i] != '\0') {
            s[i] = s[start + i];
            i++;
        }
        s[i] = '\0';
    }
}

static int str_icmp(const char *a, const char *b) {
    while (*a && *b) {
        int ca = toupper((unsigned char)*a);
        int cb = toupper((unsigned char)*b);
        if (ca != cb) return ca - cb;
        a++;
        b++;
    }
    return (unsigned char)*a - (unsigned char)*b;
}

static TrackState parse_track_state(const char *s) {
    if (str_icmp(s, "FREE") == 0)      return TRACK_FREE;
    if (str_icmp(s, "ASSIGNED") == 0)  return TRACK_ASSIGNED;
    if (str_icmp(s, "BUSY") == 0)      return TRACK_BUSY;
    if (str_icmp(s, "NONOP") == 0 ||
        str_icmp(s, "NONOPERATIONAL") == 0) return TRACK_NONOP;
    return TRACK_FREE; /* default simples */
}

/* adiciona user (com realloc) e cifra a password */
static int add_user(ManagerData *data,
                    const char *name,
                    const char *username,
                    const char *plain_password,
                    int key) {
    User *tmp = (User *)realloc(data->users,
                                (data->num_users + 1) * sizeof(User));
    if (!tmp) return 0;

    data->users = tmp;
    User *u = &data->users[data->num_users];

    strncpy(u->name, name, sizeof(u->name) - 1);
    u->name[sizeof(u->name) - 1] = '\0';

    strncpy(u->username, username, sizeof(u->username) - 1);
    u->username[sizeof(u->username) - 1] = '\0';

    u->key = key;

    char inbuf[64];
    strncpy(inbuf, plain_password, sizeof(inbuf) - 1);
    inbuf[sizeof(inbuf) - 1] = '\0';

    u->password[0] = '\0';
    if (!encrypt_data(inbuf, key, u->password)) {
        return 0;
    }

    data->num_users++;
    return 1;
}

/* adiciona track */
static int add_track(ManagerData *data,
                     int id,
                     TrackState state,
                     int train_id) {
    Track *tmp = (Track *)realloc(data->tracks,
                                  (data->num_tracks + 1) * sizeof(Track));
    if (!tmp) return 0;

    data->tracks = tmp;
    Track *t = &data->tracks[data->num_tracks];

    t->id = id;
    t->state = state;
    t->train_id = train_id;

    data->num_tracks++;
    return 1;
}

/* adiciona train */
static int add_train(ManagerData *data, int id) {
    Train *tmp = (Train *)realloc(data->trains,
                                  (data->num_trains + 1) * sizeof(Train));
    if (!tmp) return 0;

    data->trains = tmp;
    Train *tr = &data->trains[data->num_trains];

    tr->id = id;

    data->num_trains++;
    return 1;
}

static void ensure_logs_capacity(ManagerData *data, int min_capacity) {
    if (data->logs_capacity >= min_capacity) return;

    int new_cap = (data->logs_capacity == 0) ? 8 : data->logs_capacity * 2;
    if (new_cap < min_capacity) new_cap = min_capacity;

    LogRecord *tmp = (LogRecord *)realloc(data->logs,
                                          new_cap * sizeof(LogRecord));
    if (!tmp) return;

    data->logs = tmp;
    data->logs_capacity = new_cap;
}

/* ---------- USAC11: carregar ficheiro ---------- */

int manager_initial_setup(const char *filename, ManagerData *data) {
    if (!filename || !data) return 0;

    FILE *f = fopen(filename, "r");
    if (!f) {
        return 0;
    }

    manager_init_empty(data);

    char line[256];

    while (fgets(line, sizeof(line), f) != NULL) {
        trim(line);
        if (line[0] == '\0') continue;
        if (line[0] == '#')  continue;

        char *p = line;
        char *type = strtok(p, ";\n");
        if (!type) continue;
        trim(type);

        if (str_icmp(type, "USER") == 0) {
            /* USER;Name;username;PASSWORD;key */
            char *name     = strtok(NULL, ";\n");
            char *username = strtok(NULL, ";\n");
            char *pass     = strtok(NULL, ";\n");
            char *keyStr   = strtok(NULL, ";\n");

            if (!name || !username || !pass || !keyStr) {
                fclose(f);
                manager_free(data);
                return 0;
            }

            trim(name);
            trim(username);
            trim(pass);
            trim(keyStr);

            int key = atoi(keyStr);

            if (!add_user(data, name, username, pass, key)) {
                fclose(f);
                manager_free(data);
                return 0;
            }
        }
        else if (str_icmp(type, "SENSOR") == 0) {
            /* SENSOR;TEMP;BUFFER_LEN;50;MM_WIN;5 */
            char *which    = strtok(NULL, ";\n");
            char *bufTok   = strtok(NULL, ";\n");
            char *bufStr   = strtok(NULL, ";\n");
            char *winTok   = strtok(NULL, ";\n");
            char *winStr   = strtok(NULL, ";\n");

            if (!which || !bufTok || !bufStr || !winTok || !winStr) {
                fclose(f);
                manager_free(data);
                return 0;
            }

            trim(which);
            trim(bufTok);
            trim(bufStr);
            trim(winTok);
            trim(winStr);

            int bufLen = atoi(bufStr);
            int winLen = atoi(winStr);

            if (str_icmp(which, "TEMP") == 0) {
                data->sensors.temp.buffer_length = bufLen;
                data->sensors.temp.mm_window     = winLen;
            } else if (str_icmp(which, "HUM") == 0) {
                data->sensors.hum.buffer_length = bufLen;
                data->sensors.hum.mm_window     = winLen;
            }
        }
        else if (str_icmp(type, "TRACK") == 0) {
            /* TRACK;id;state;train_id */
            char *idStr      = strtok(NULL, ";\n");
            char *stateStr   = strtok(NULL, ";\n");
            char *trainIdStr = strtok(NULL, ";\n");

            if (!idStr || !stateStr || !trainIdStr) {
                fclose(f);
                manager_free(data);
                return 0;
            }

            trim(idStr);
            trim(stateStr);
            trim(trainIdStr);

            int id = atoi(idStr);
            int train_id = atoi(trainIdStr);
            TrackState st = parse_track_state(stateStr);

            if (!add_track(data, id, st, train_id)) {
                fclose(f);
                manager_free(data);
                return 0;
            }
        }
        else if (str_icmp(type, "TRAIN") == 0) {
            /* TRAIN;id */
            char *idStr = strtok(NULL, ";\n");
            if (!idStr) {
                fclose(f);
                manager_free(data);
                return 0;
            }
            trim(idStr);
            int id = atoi(idStr);
            if (!add_train(data, id)) {
                fclose(f);
                manager_free(data);
                return 0;
            }
        }
        else if (str_icmp(type, "LOGCFG") == 0) {
            /* LOGCFG;NEXT_ID;1  */
            char *what   = strtok(NULL, ";\n");
            char *value  = strtok(NULL, ";\n");
            if (!what || !value) {
                fclose(f);
                manager_free(data);
                return 0;
            }
            trim(what);
            trim(value);
            if (str_icmp(what, "NEXT_ID") == 0) {
                data->next_log_id = atoi(value);
            }
        }
        else {
            /* linha desconhecida -> ignoro */
        }
    }

    fclose(f);
    ensure_logs_capacity(data, 8);
    return 1;
}
