#include "data_structures.h"
#include <string.h>

/**
 * @brief Converte uma string para UserRole
 *
 * @param role_str String representando o papel
 * @return UserRole correspondente
 */
UserRole string_to_role(const char* role_str) {
    if (strcmp(role_str, "ADMIN") == 0) return ROLE_ADMIN;
    if (strcmp(role_str, "OPERATOR") == 0) return ROLE_OPERATOR;
    if (strcmp(role_str, "TRAFFIC_MANAGER") == 0) return ROLE_TRAFFIC_MANAGER;
    if (strcmp(role_str, "VIEWER") == 0) return ROLE_VIEWER;
    return ROLE_VIEWER; // Default
}

/**
 * @brief Converte UserRole para string
 *
 * @param role Papel do utilizador
 * @return String representando o papel
 */
const char* role_to_string(UserRole role) {
    switch(role) {
        case ROLE_ADMIN: return "Administrador";
        case ROLE_OPERATOR: return "Operador";
        case ROLE_TRAFFIC_MANAGER: return "Gestor de Tráfego";
        case ROLE_VIEWER: return "Visualizador";
        default: return "Desconhecido";
    }
}

/**
 * @brief Converte TrackState para string
 *
 * @param state Estado da via
 * @return String representando o estado
 */
const char* track_state_to_string(TrackState state) {
    switch(state) {
        case TRACK_FREE: return "Livre";
        case TRACK_BUSY: return "Ocupada";
        case TRACK_ASSIGNED: return "Atribuída";
        case TRACK_INOPERATIVE: return "Inoperacional";
        default: return "Desconhecido";
    }
}

/**
 * @brief Converte string para TrackState
 *
 * @param state_str String representando o estado
 * @return TrackState correspondente
 */
TrackState string_to_track_state(const char* state_str) {
    if (strcmp(state_str, "FREE") == 0) return TRACK_FREE;
    if (strcmp(state_str, "BUSY") == 0) return TRACK_BUSY;
    if (strcmp(state_str, "ASSIGNED") == 0) return TRACK_ASSIGNED;
    if (strcmp(state_str, "INOPERATIVE") == 0) return TRACK_INOPERATIVE;
    return TRACK_FREE; // Default
}

/**
 * @brief Cria uma nova entrada de log
 *
 * @param id Identificador do log
 * @param username Nome do utilizador
 * @param action Ação executada
 * @return LogEntry preenchida
 */
LogEntry create_log_entry(int id, const char* username, const char* action) {
    LogEntry entry;
    entry.id = id;
    strncpy(entry.username, username, MAX_USERNAME_LEN - 1);
    entry.username[MAX_USERNAME_LEN - 1] = '\0';
    strncpy(entry.action, action, MAX_ACTION_LEN - 1);
    entry.action[MAX_ACTION_LEN - 1] = '\0';
    entry.timestamp = time(NULL);
    return entry;
}

/**
 * @brief Cria uma nova via
 *
 * @param id Identificador da via
 * @param state Estado inicial
 * @param trainId ID do comboio (-1 se livre)
 * @return Track preenchida
 */
Track create_track(int id, TrackState state, int trainId) {
    Track track;
    track.id = id;
    track.state = state;
    track.trainId = trainId;
    return track;
}

/**
 * @brief Cria um novo utilizador
 *
 * @param name Nome completo
 * @param username Nome de utilizador
 * @param password Password em texto claro
 * @param cipherKey Chave para cifra
 * @param role Papel do utilizador
 * @return User preenchido (password NÃO encriptada)
 */
User create_user(const char* name, const char* username,
                 const char* password, int cipherKey, UserRole role) {
    User user;
    strncpy(user.name, name, MAX_NAME_LEN - 1);
    user.name[MAX_NAME_LEN - 1] = '\0';
    strncpy(user.username, username, MAX_USERNAME_LEN - 1);
    user.username[MAX_USERNAME_LEN - 1] = '\0';
    strncpy(user.password, password, MAX_PASSWORD_LEN - 1);
    user.password[MAX_PASSWORD_LEN - 1] = '\0';
    user.cipherKey = cipherKey;
    user.role = role;
    return user;
}

/**
 * @brief Inicializa um buffer de sensor
 *
 * @param buffer Ponteiro para o buffer
 * @param length Tamanho do buffer
 * @param window Tamanho da janela para mediana
 * @return SensorBuffer inicializado
 */
SensorBuffer init_sensor_buffer(int* buffer, int length, int window) {
    SensorBuffer sb;
    sb.buffer = buffer;
    sb.length = length;
    sb.window = window;
    sb.nelem = 0;
    sb.head = 0;
    sb.tail = 0;
    return sb;
}

/**
 * @brief Verifica se uma via é válida
 *
 * @param track Ponteiro para a via
 * @return 1 se válida, 0 caso contrário
 */
int is_valid_track(const Track* track) {
    if (track == NULL) return 0;
    if (track->id < 1 || track->id > MAX_TRACKS) return 0;
    if (track->state < TRACK_FREE || track->state > TRACK_INOPERATIVE) return 0;
    return 1;
}

/**
 * @brief Verifica se um utilizador é válido
 *
 * @param user Ponteiro para o utilizador
 * @return 1 se válido, 0 caso contrário
 */
int is_valid_user(const User* user) {
    if (user == NULL) return 0;
    if (strlen(user->username) == 0) return 0;
    if (strlen(user->password) == 0) return 0;
    if (user->cipherKey < 1 || user->cipherKey > 26) return 0;
    return 1;
}

/**
 * @brief Obtém o estado do LED baseado no estado da via
 *
 * @param state Estado da via
 * @return String com comando LED (RE, YE, GE, RB)
 */
const char* get_led_command(TrackState state) {
    switch(state) {
        case TRACK_FREE: return "GE";
        case TRACK_BUSY: return "RE";
        case TRACK_ASSIGNED: return "YE";
        case TRACK_INOPERATIVE: return "RB";
        default: return "GE";
    }
}