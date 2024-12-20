package com.example.chunkmaster;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/chunkMaster")
public class ChunkMasterController {

    private ChunkMasterState state = new ChunkMasterState();

    // Map of chunk server URLs and the number of chunks they hold
    private final Map<String, Integer> chunkServers = new HashMap<>();

    @PostMapping("/chunkStatus")
    public ResponseEntity<String> chunkServerStatus(@RequestBody Heartbeat heartbeat) {
        state.updateChunkServerState(heartbeat);
        return ResponseEntity.ok("STATUS_RECEIVED");
    }

    // Endpoint to map file chunks to chunk servers
    @PostMapping("/mapFile")
    public ResponseEntity<Response<FileMappingResponse, String>> mapFile(
            @RequestParam("fileName") String fileName,
            @RequestBody ArrayList<ChunkMetadata> chunks,
            @RequestParam("numCopies") int numCopies
    ) {
        Pair<Optional<FileMappingResponse>, Optional<Error>> pair = state.writeFile(fileName, chunks, numCopies);
        if (pair.first().isPresent()) {
            return ResponseEntity.ok().body(FileMappingResponse.successFulResponse(pair));
        } else {
            return ResponseEntity.badRequest().body(FileMappingResponse.errorResponse(pair));
        }
    }

    // Endpoint to get all file-to-chunk-server mapping
    @GetMapping("/getAllFileMapping")
    public ResponseEntity<Map<String, Map<String, List<String>>>> getAllFileMapping() {
        Map<String, Map<String, List<String>>> mappings = state.allMapping();
        if (mappings.isEmpty()) {
            return ResponseEntity.ok().body(null);
        }
        return ResponseEntity.ok(mappings);
    }

    // Endpoint to get chunk servers for a file
    @GetMapping("/getFileMapping")
    public ResponseEntity<Response<FileMappingResponse, String>> getFileMapping(@RequestParam("fileName") String fileName) {
        Pair<Optional<FileMappingResponse>, Optional<Error>> pair = state.readFile(fileName);
        if (pair.first().isPresent()) {
            return ResponseEntity.ok(FileMappingResponse.successFulResponse(pair));
        } else {
            return ResponseEntity.badRequest().body(FileMappingResponse.errorResponse(pair));
        }
    }

    // Endpoint to get list of servers
    @GetMapping("/getServerList")
    public ResponseEntity<List<String>> getServers() {
        return ResponseEntity.ok(new ArrayList<>(state.chunkServersByNetworkAddress().keySet()));
    }

    // Endpoint to get chunk servers map
    @GetMapping("/getChunkServersMap")
    public ResponseEntity<Map<String, Integer>> getChunkServers() {
        return ResponseEntity.ok(chunkServers);
    }
}
