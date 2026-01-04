.section .text
.global set_track_free_asm

# rdi = endereço da struct Track (passado pelo C)
set_track_free_asm:
    # 1. Definir track->state = TRACK_FREE (0)
    # O campo 'state' está 4 bytes após o início da struct
    movl $0, 4(%rdi)

    # 2. Definir track->train_id = -1
    # O campo 'train_id' está 8 bytes após o início da struct
    movl $-1, 8(%rdi)

    ret