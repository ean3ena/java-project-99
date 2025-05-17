package hexlet.code.repository;

import hexlet.code.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    Optional<Task> findById(Long id);

    List<Task> findAllByAssigneeId(Long id);
    List<Task> findAllByTaskStatusId(Long id);

    @Query("SELECT t FROM #{#entityName} t JOIN t.labels l WHERE l.id = :labelId")
    List<Task> findAllByLabelId(@Param("labelId") Long id);
}
