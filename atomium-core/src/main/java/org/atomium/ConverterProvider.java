package org.atomium;

import com.google.common.reflect.TypeToken;

/**
 * @author Blackrush
 */
public interface ConverterProvider {
    TypeToken<?>[] getExtracted();
    TypeToken<?>[] getExported();

    ConverterInterface get();
}
