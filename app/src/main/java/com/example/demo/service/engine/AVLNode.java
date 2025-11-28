package com.example.demo.service.engine;

import java.io.Serializable;

public class AVLNode<K extends Comparable<K>, V> implements Serializable {
    K key;
    V value;
    AVLNode<K, V> left, right;
    int height;

    public AVLNode(K key, V value) {
        this.key = key;
        this.value = value;
        this.height = 1;
    }
}