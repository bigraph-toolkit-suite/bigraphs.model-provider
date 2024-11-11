package org.bigraphs.model.provider;

import java.util.function.Supplier;

public interface BModelProviderSupport {
    default Supplier<String> createNameSupplier(final String prefix) {
        return new Supplier<>() {
            private int id = 0;

            @Override
            public String get() {
                return prefix + id++;
            }
        };
    }
}
