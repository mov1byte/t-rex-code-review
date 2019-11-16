package morcheka_task.controller;

import morcheka_task.model.Note;
import morcheka_task.payload.CreateNoteRequest;
import morcheka_task.payload.NoteView;
import morcheka_task.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class NoteController {

    private static final String USER_NOT_FOUND = "The user is not found";

    private final UserService userService;

    public NoteController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all") // notes/all
    public ResponseEntity getAllNotes() {
        var username = extractUserNameFormSecurityContext();
        var userOptional = userService.findUserByUsername(username);
        if (userOptional.isPresent()) {
            var user = userOptional.orElseThrow();
            var notesView = user.getNotes().stream()
                    .map(note -> NoteView.builder()
                            .firstName(note.getFirstName())
                            .lastName(note.getLastName())
                            .address(note.getAddress())
                            .phone(note.getPhone())
                            .id(note.getId())
                    ).collect(Collectors.toUnmodifiableList());
            return ResponseEntity.status(HttpStatus.OK).body(notesView);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(USER_NOT_FOUND);
        }
    }

    // notes/delete
    @GetMapping("/delete")
    @Transactional
    public ResponseEntity deleteNote(@RequestParam("id") long id) {
        var username = extractUserNameFormSecurityContext();
        var userOptional = userService.findUserByUsername(username);
        if (userOptional.isPresent()) {
            var user = userOptional.orElseThrow();
            user.getNotes().stream()
                    .filter(note -> note.getId() == id)
                    .findFirst()
                    .ifPresent(note -> userService.deleteNoteFromUser(user, note));
            return ResponseEntity.status(HttpStatus.OK).body("The note is successfully deleted");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(USER_NOT_FOUND);
        }
    }

    // notes/save
    @PostMapping("/save")
    public ResponseEntity saveNote(@Valid @RequestBody CreateNoteRequest noteRequest) {

        // refactor this

        var username = extractUserNameFormSecurityContext();
        var userOptional = userService.findUserByUsername(username);
        if (userOptional.isPresent()) {
            var user = userOptional.orElseThrow();
            var newNote = new Note();

            newNote.setFirstName(noteRequest.getFirstName());
            newNote.setLastName(noteRequest.getLastName());
            newNote.setAddress(noteRequest.getAddress());
            newNote.setPhone(noteRequest.getPhone());

            // should check if provided note id already exists in database!
            if (noteRequest.getId() != null) {
                newNote.setId(noteRequest.getId());
            }

            var notes = user.getNotes();

            if (notes != null) {
                for (int i = 0; i < notes.size(); i++) {
                    if (notes.get(i).getId() == newNote.getId()) {
                        notes.set(i, newNote);
                        break;
                    }
                }

                notes.add(newNote);
            } else {
                var newNotes = new ArrayList<Note>();
                newNotes.add(newNote);
                user.setNotes(newNotes);
            }

            userService.updateUser(user);

            return ResponseEntity.status(HttpStatus.OK).body("The note is successfully saved");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(USER_NOT_FOUND);
        }
    }

    @Nonnull
    private String extractUserNameFormSecurityContext() {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof UserDetails
                ? ((UserDetails) principal).getUsername()
                : principal.toString();
    }
}
