package ru.dsckibin.util.git;

public class Commit {
    private final String hash;
    private final String message;

    public Commit(String hash, String message) {
        this.hash = hash;
        this.message = message;
    }

    public String getHash() {
        return hash;
    }

    @Override
    public String toString() {
        return "Commit{" +
                "hash='" + hash + '\'' +
                ", " + message + '\'' +
                '}';
    }
}
