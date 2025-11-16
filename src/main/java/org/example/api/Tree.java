package org.example.api;

import java.util.Set;

public record Tree(Device node, Set<Tree> children) {
}
