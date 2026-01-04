#ifndef LIGHTSIGNS_COMM_H
#define LIGHTSIGNS_COMM_H

#include "../USAC11/manager_setup.h"

/* Funções Assembly - novas para USAC14 */
int format_light_command(char* op, int track_id, char* output);
char* control_light_asm(int fd, int track_id, int state);
int format_light_command_c(char* op, int track_id, char* output);

/* Funções principais */
int control_track_light(int fd, int track_id, TrackState state);
int control_track_light_asm(int fd, int track_id, TrackState state);
int control_track_light_hybrid(int fd, int track_id, TrackState state);
int send_light_command(int fd, const char *cmd);
int update_all_lights(int fd, const Track *tracks, int num_tracks);
const char* state_to_command(TrackState state);

#endif