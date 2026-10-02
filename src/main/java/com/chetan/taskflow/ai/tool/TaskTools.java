package com.chetan.taskflow.ai.tool;

import com.chetan.taskflow.ai.context.RequestContext;
import com.chetan.taskflow.ai.dto.CreateTaskRequest;
import com.chetan.taskflow.ai.dto.TaskPriority;
import com.chetan.taskflow.ai.dto.TaskResponse;
import com.chetan.taskflow.ai.dto.TaskStatus;
import com.chetan.taskflow.ai.dto.UpdateTaskRequest;
import com.chetan.taskflow.ai.dto.UpdateTaskStatusRequest;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

@Component
public class TaskTools {

    private final RestClient restClient;
    private final RequestContext requestContext;

    public TaskTools(
            RestClient.Builder restClientBuilder,
            RequestContext requestContext) {

        this.restClient = restClientBuilder
                .baseUrl("http://localhost:8081")
                .build();

        this.requestContext = requestContext;
    }

    // -------------------------------------------------------
    // READ ALL
    // -------------------------------------------------------

    @Tool(description = """
            Returns all current TaskFlow tasks belonging to the
            authenticated user.
            """)
    public List<TaskResponse> getTasks() {

        System.out.println(">>> getTasks TOOL EXECUTED");

        return fetchTasks();
    }

    // -------------------------------------------------------
    // FILTER BY PRIORITY
    // -------------------------------------------------------

    @Tool(description = """
            Returns the authenticated user's TaskFlow tasks
            having the specified priority.
            """)
    public List<TaskResponse> getTasksByPriority(TaskPriority priority) {

        System.out.println(
                ">>> getTasksByPriority TOOL EXECUTED: " + priority
        );

        return fetchTasks().stream()
                .filter(task -> task.priority() == priority)
                .toList();
    }

    // -------------------------------------------------------
    // FILTER BY STATUS
    // -------------------------------------------------------

    @Tool(description = """
            Returns the authenticated user's TaskFlow tasks
            having the specified status.
            Valid statuses are TODO, IN_PROGRESS and COMPLETED.
            """)
    public List<TaskResponse> getTasksByStatus(TaskStatus status) {

        System.out.println(
                ">>> getTasksByStatus TOOL EXECUTED: " + status
        );

        return fetchTasks().stream()
                .filter(task -> task.status() == status)
                .toList();
    }

    // -------------------------------------------------------
    // GET ONE
    // -------------------------------------------------------

    @Tool(description = """
            Returns a specific TaskFlow task belonging to the
            authenticated user. Use the task ID to identify it.
            """)
    public TaskResponse getTaskById(Long id) {

        System.out.println(
                ">>> getTaskById TOOL EXECUTED: " + id
        );

        return restClient
                .get()
                .uri("/api/tasks/{id}", id)
                .header("Authorization", authorizationHeader())
                .retrieve()
                .body(TaskResponse.class);
    }

    // -------------------------------------------------------
    // CREATE
    // -------------------------------------------------------

    @Tool(description = """
            Creates a new TaskFlow task for the authenticated user.
            Use this only when the user explicitly asks to create
            or add a task.
            """)
    public TaskResponse createTask(
            String title,
            String description,
            TaskPriority priority,
            LocalDate dueDate) {

        System.out.println(
                ">>> createTask TOOL EXECUTED: " + title
        );

        CreateTaskRequest request = new CreateTaskRequest(
                title,
                description,
                priority,
                dueDate
        );

        return restClient
                .post()
                .uri("/api/tasks")
                .header("Authorization", authorizationHeader())
                .body(request)
                .retrieve()
                .body(TaskResponse.class);
    }

    // -------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------

    @Tool(description = """
            Updates an existing TaskFlow task belonging to the
            authenticated user. Use this only when the user
            explicitly asks to modify an existing task.
            """)
    public TaskResponse updateTask(
            Long id,
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDate) {

        System.out.println(
                ">>> updateTask TOOL EXECUTED: " + id
        );

        UpdateTaskRequest request = new UpdateTaskRequest(
                title,
                description,
                status,
                priority,
                dueDate
        );

        return restClient
                .put()
                .uri("/api/tasks/{id}", id)
                .header("Authorization", authorizationHeader())
                .body(request)
                .retrieve()
                .body(TaskResponse.class);
    }

    // -------------------------------------------------------
    // CHANGE STATUS
    // -------------------------------------------------------

    @Tool(description = """
            Changes only the status of an existing TaskFlow task.
            Valid statuses are TODO, IN_PROGRESS and COMPLETED.
            Use this when the user asks to start, complete,
            reopen or otherwise change a task's status.
            """)
    public TaskResponse changeTaskStatus(
            Long id,
            TaskStatus status) {

        System.out.println(
                ">>> changeTaskStatus TOOL EXECUTED: "
                        + id + " -> " + status
        );

        UpdateTaskStatusRequest request =
                new UpdateTaskStatusRequest(status);

        return restClient
                .patch()
                .uri("/api/tasks/{id}/status", id)
                .header("Authorization", authorizationHeader())
                .body(request)
                .retrieve()
                .body(TaskResponse.class);
    }

    // -------------------------------------------------------
    // DELETE
    // -------------------------------------------------------

    @Tool(description = """
            Permanently deletes a TaskFlow task belonging to the
            authenticated user. Use this only after the user has
            explicitly requested deletion of a specific task.
            """)
    public String deleteTask(Long id) {

        System.out.println(
                ">>> deleteTask TOOL EXECUTED: " + id
        );

        restClient
                .delete()
                .uri("/api/tasks/{id}", id)
                .header("Authorization", authorizationHeader())
                .retrieve()
                .toBodilessEntity();

        return "Task " + id + " was deleted successfully.";
    }

    // -------------------------------------------------------
    // INTERNAL HELPERS - NOT EXPOSED AS AI TOOLS
    // -------------------------------------------------------

    private List<TaskResponse> fetchTasks() {

        List<TaskResponse> tasks = restClient
                .get()
                .uri("/api/tasks")
                .header("Authorization", authorizationHeader())
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                List<TaskResponse>>() {
                        }
                );

        return tasks != null ? tasks : List.of();
    }

    private String authorizationHeader() {

        String authorization =
                requestContext.getAuthorizationHeader();

        if (authorization == null ||
                authorization.isBlank()) {

            throw new IllegalStateException(
                    "Authorization header is missing"
            );
        }

        return authorization;
    }
}