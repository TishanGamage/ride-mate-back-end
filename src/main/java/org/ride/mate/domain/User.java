}
    }
        updatedAt = LocalDateTime.now();
    protected void onUpdate() {
    @PreUpdate

    }
        updatedAt = LocalDateTime.now();
        createdAt = LocalDateTime.now();
    protected void onCreate() {
    @PrePersist

    private UserCredentials userCredentials;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)

    private UserProfile userProfile;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)

    private LocalDateTime updatedAt;
    @Column(nullable = false)

    private LocalDateTime createdAt;
    @Column(nullable = false, updatable = false)

    private Boolean isActive = true;
    @Column(nullable = false)

    private String phoneNumber;
    @Column(nullable = false, length = 20)

    private String email;
    @Column(nullable = false, unique = true, length = 100)

    private String username;
    @Column(nullable = false, unique = true, length = 50)

    private Long id;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id

public class User {
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "user")
@Entity

import java.time.LocalDateTime;

import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;


