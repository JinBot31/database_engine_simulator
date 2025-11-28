package com.example.demo.service.engine;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

public class AVLTree<K extends Comparable<K>, V> implements Serializable {
    private AVLNode<K, V> root;

    private int height(AVLNode<K, V> node) {
        return node == null ? 0 : node.height;
    }

    private int getBalance(AVLNode<K, V> node) {
        return node == null ? 0 : height(node.left) - height(node.right);
    }

    private AVLNode<K, V> rotateRight(AVLNode<K, V> y) {
        AVLNode<K, V> x = y.left;
        AVLNode<K, V> T2 = x.right;

        x.right = y;
        y.left = T2;

        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;

        return x;
    }

    private AVLNode<K, V> rotateLeft(AVLNode<K, V> x) {
        AVLNode<K, V> y = x.right;
        AVLNode<K, V> T2 = y.left;

        y.left = x;
        x.right = T2;

        x.height = Math.max(height(x.left), height(x.right)) + 1;
        y.height = Math.max(height(y.left), height(y.right)) + 1;

        return y;
    }

    public void insert(K key, V value) {
        root = insertRec(root, key, value);
    }

    private AVLNode<K, V> insertRec(AVLNode<K, V> node, K key, V value) {
        if (node == null) return new AVLNode<>(key, value);

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = insertRec(node.left, key, value);
        } else if (cmp > 0) {
            node.right = insertRec(node.right, key, value);
        } else {
            node.value = value; // Actualizar si existe
            return node;
        }

        node.height = 1 + Math.max(height(node.left), height(node.right));
        int balance = getBalance(node);

        // Rotaciones para balanceo
        if (balance > 1 && key.compareTo(node.left.key) < 0)
            return rotateRight(node);

        if (balance < -1 && key.compareTo(node.right.key) > 0)
            return rotateLeft(node);

        if (balance > 1 && key.compareTo(node.left.key) > 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

        if (balance < -1 && key.compareTo(node.right.key) < 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node;
    }

    public V search(K key) {
        AVLNode<K, V> result = searchRec(root, key);
        return result != null ? result.value : null;
    }

    private AVLNode<K, V> searchRec(AVLNode<K, V> node, K key) {
        if (node == null) return null;

        int cmp = key.compareTo(node.key);
        if (cmp == 0) return node;
        if (cmp < 0) return searchRec(node.left, key);
        return searchRec(node.right, key);
    }

    public void delete(K key) {
        root = deleteRec(root, key);
    }

    private AVLNode<K, V> deleteRec(AVLNode<K, V> node, K key) {
        if (node == null) return node;

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = deleteRec(node.left, key);
        } else if (cmp > 0) {
            node.right = deleteRec(node.right, key);
        } else {
            if (node.left == null || node.right == null) {
                node = (node.left != null) ? node.left : node.right;
            } else {
                AVLNode<K, V> temp = minValueNode(node.right);
                node.key = temp.key;
                node.value = temp.value;
                node.right = deleteRec(node.right, temp.key);
            }
        }

        if (node == null) return node;

        node.height = Math.max(height(node.left), height(node.right)) + 1;
        int balance = getBalance(node);

        if (balance > 1 && getBalance(node.left) >= 0)
            return rotateRight(node);

        if (balance > 1 && getBalance(node.left) < 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

        if (balance < -1 && getBalance(node.right) <= 0)
            return rotateLeft(node);

        if (balance < -1 && getBalance(node.right) > 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node;
    }

    private AVLNode<K, V> minValueNode(AVLNode<K, V> node) {
        AVLNode<K, V> current = node;
        while (current.left != null)
            current = current.left;
        return current;
    }

    public List<V> rangeQuery(K min, K max) {
        List<V> results = new ArrayList<>();
        rangeQueryRec(root, min, max, results);
        return results;
    }

    private void rangeQueryRec(AVLNode<K, V> node, K min, K max, List<V> results) {
        if (node == null) return;

        if (min.compareTo(node.key) < 0)
            rangeQueryRec(node.left, min, max, results);

        if (min.compareTo(node.key) <= 0 && max.compareTo(node.key) >= 0)
            results.add(node.value);

        if (max.compareTo(node.key) > 0)
            rangeQueryRec(node.right, min, max, results);
    }

    public void inOrderTraversal(List<V> results) {
        inOrderRec(root, results);
    }

    private void inOrderRec(AVLNode<K, V> node, List<V> results) {
        if (node != null) {
            inOrderRec(node.left, results);
            results.add(node.value);
            inOrderRec(node.right, results);
        }
    }
}
