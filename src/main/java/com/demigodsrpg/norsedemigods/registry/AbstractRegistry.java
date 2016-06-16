/*
 * Copyright (c) 2015 Demigods RPG
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.demigodsrpg.norsedemigods.registry;

import com.demigodsrpg.norsedemigods.NorseDemigods;
import com.demigodsrpg.norsedemigods.Saveable;
import com.demigodsrpg.norsedemigods.util.FJsonSection;
import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ResultOfMethodCallIgnored")
public abstract class AbstractRegistry<T extends Saveable> {
    protected final Cache<String, T> REGISTERED_DATA;

    // -- FILE -- //
    private final File FOLDER;
    private final boolean PRETTY;

    public AbstractRegistry(NorseDemigods backend, String folder, boolean pretty) {
        REGISTERED_DATA = CacheBuilder.newBuilder().concurrencyLevel(4).expireAfterAccess(3, TimeUnit.MINUTES).
                build(CacheLoader.from(new Function<String, T>() {
                    @Override
                    public T apply(@Nullable String s) {
                        return loadFromDb(s, null);
                    }
                }));
        FOLDER = new File(backend.getDataFolder().getPath() + "/" + folder + "/");
        PRETTY = pretty;
    }

    public Optional<T> fromKey(String key) {
        if (!REGISTERED_DATA.asMap().containsKey(key)) {
            loadFromDb(key, null);
        }
        return Optional.ofNullable(REGISTERED_DATA.asMap().getOrDefault(key, null));
    }

    public T register(T value) {
        REGISTERED_DATA.asMap().put(value.getKey(), value);
        saveToDb(value.getKey());
        return value;
    }

    public T put(String key, T value) {
        REGISTERED_DATA.asMap().put(key, value);
        saveToDb(key);
        return value;
    }

    public void remove(String key) {
        REGISTERED_DATA.asMap().remove(key);
        removeFile(key);
    }

    private void createFile(File file) {
        try {
            FOLDER.mkdirs();
            file.createNewFile();
        } catch (Exception oops) {
            oops.printStackTrace();
        }
    }

    public void removeFile(String key) {
        File file = new File(FOLDER.getPath() + "/" + key + ".json");
        if (file.exists()) {
            file.delete();
        }
    }

    public void saveToDb(String key) {
        if (REGISTERED_DATA.asMap().containsKey(key)) {
            File file = new File(FOLDER.getPath() + "/" + key + ".json");
            if (!(file.exists())) {
                createFile(file);
            }
            Gson gson = PRETTY ? new GsonBuilder().setPrettyPrinting().create() : new GsonBuilder().create();
            String json = gson.toJson(REGISTERED_DATA.asMap().get(key).serialize());
            try {
                PrintWriter writer = new PrintWriter(file);
                writer.print(json);
                writer.close();
            } catch (Exception oops) {
                oops.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public T loadFromDb(String key, T defaultVal) {
        T loaded = defaultVal;
        Gson gson = new GsonBuilder().create();
        try {
            File file = new File(FOLDER.getPath() + "/" + key + ".json");
            if (file.exists()) {
                FileInputStream inputStream = new FileInputStream(file);
                InputStreamReader reader = new InputStreamReader(inputStream);
                FJsonSection section = new FJsonSection(gson.fromJson(reader, Map.class));
                loaded = fromFJsonSection(key, section);
                reader.close();
            }
        } catch (Exception oops) {
            oops.printStackTrace();
        }
        return loaded;
    }

    @SuppressWarnings("ConstantConditions")
    public ConcurrentMap<String, T> getFromDb() {
        ConcurrentMap<String, T> MAP = new ConcurrentHashMap<>();
        try {
            for (File file : FOLDER.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    String key = file.getName().replace(".json", "");
                    T val = loadFromDb(key, null);
                    if (val != null) {
                        MAP.put(key, val);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return MAP;
    }

    public void purge() {
        REGISTERED_DATA.asMap().keySet().forEach(this::removeFile);
        REGISTERED_DATA.asMap().clear();
    }

    protected abstract T fromFJsonSection(String key, FJsonSection section);
}
