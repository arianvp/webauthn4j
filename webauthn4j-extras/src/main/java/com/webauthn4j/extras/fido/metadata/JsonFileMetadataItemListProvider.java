/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webauthn4j.extras.fido.metadata;

import com.webauthn4j.registry.Registry;
import com.webauthn4j.response.attestation.authenticator.AAGUID;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonFileMetadataItemListProvider implements MetadataItemListProvider<MetadataItem> {

    private Registry registry;
    private List<Path> paths = Collections.emptyList();
    private Map<AAGUID, List<MetadataItem>> cachedMetadataItems;

    public JsonFileMetadataItemListProvider(Registry registry) {
        this.registry = registry;
    }

    @Override
    public Map<AAGUID, List<MetadataItem>> provide() {
        if (cachedMetadataItems == null) {
            cachedMetadataItems =
                    paths.stream()
                            .map(path -> new MetadataItemImpl(readJsonFile(path)))
                            .collect(Collectors.groupingBy(item -> extractAAGUID(item.getMetadataStatement())));
        }
        return cachedMetadataItems;
    }

    private AAGUID extractAAGUID(MetadataStatement metadataStatement) {
        switch (metadataStatement.getProtocolFamily()) {
            case "fido2":
                return new AAGUID(metadataStatement.getAaguid());
            case "u2f":
                return AAGUID.ZERO;
            case "uaf":
            default:
                return AAGUID.NULL;
        }
    }

    MetadataStatement readJsonFile(Path path) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            return registry.getJsonMapper().readValue(inputStream, MetadataStatement.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load a metadata statement json file", e);
        }
    }
}
