#ifndef MOVING_MEDIAN_H
#define MOVING_MEDIAN_H

#include <Arduino.h>

template<typename T, int N>
class MovingMedian {
private:
    T buffer[N];
    T sorted[N];
    int index;
    int count;
    bool filled;

    void sortArray(T arr[], int n) {
        // Simple bubble sort for small arrays
        for (int i = 0; i < n-1; i++) {
            for (int j = 0; j < n-i-1; j++) {
                if (arr[j] > arr[j+1]) {
                    T temp = arr[j];
                    arr[j] = arr[j+1];
                    arr[j+1] = temp;
                }
            }
        }
    }

public:
    MovingMedian() {
        reset();
    }

    void reset() {
        index = 0;
        count = 0;
        filled = false;
        for (int i = 0; i < N; i++) {
            buffer[i] = 0;
            sorted[i] = 0;
        }
    }

    void add(T value) {
        buffer[index] = value;
        index = (index + 1) % N;

        if (!filled && count < N) {
            count++;
            if (count == N) filled = true;
        }
    }

    T getMedian() {
        if (count == 0) return 0;

        // Copy buffer to sorted array
        int elements = filled ? N : count;
        for (int i = 0; i < elements; i++) {
            sorted[i] = buffer[i];
        }

        // Sort the array
        sortArray(sorted, elements);

        // Calculate median
        if (elements % 2 == 0) {
            // Even number of elements: average of two middle values
            int mid = elements / 2;
            return (sorted[mid - 1] + sorted[mid]) / 2;
        } else {
            // Odd number of elements: middle value
            return sorted[elements / 2];
        }
    }

    int getCount() const {
        return count;
    }

    bool isFilled() const {
        return filled;
    }

    void getBuffer(T output[], int &size) const {
        size = count;
        for (int i = 0; i < count; i++) {
            output[i] = buffer[i];
        }
    }
};

#endif // MOVING_MEDIAN_H