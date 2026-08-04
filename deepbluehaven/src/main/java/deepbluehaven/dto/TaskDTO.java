package deepbluehaven.dto;

import deepbluehaven.pojo.enums.TaskStatus;

public class TaskDTO {

    public static class Response {
        private Long id;
        private String taskCode;
        private Long roomId;
        private String roomNumber;
        private String roomType;
        private String action;
        private String priority;
        private String priorityClass;
        private TaskStatus status;
        private String statusLabel;
        private String statusClass;
        private String timeText;
        private String assignedByName;
        private String resultText;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public String getTaskCode() { 
            return taskCode; }
        public void setTaskCode(String taskCode) { 
            this.taskCode = taskCode; }

        public Long getRoomId() { 
            return roomId; }
        public void setRoomId(Long roomId) { 
            this.roomId = roomId; }

        public String getRoomNumber() { 
            return roomNumber; }
        public void setRoomNumber(String roomNumber) { 
            this.roomNumber = roomNumber; }

        public String getRoomType() { 
            return roomType; }
        public void setRoomType(String roomType) { 
            this.roomType = roomType; }

        public String getAction() { 
            return action; }
        public void setAction(String action) { 
            this.action = action; }

        public String getPriority() { 
            return priority; }
        public void setPriority(String priority) { 
            this.priority = priority; }

        public String getPriorityClass() { 
            return priorityClass; }
        public void setPriorityClass(String priorityClass) { 
            this.priorityClass = priorityClass; }

        public TaskStatus getStatus() { 
            return status; }
        public void setStatus(TaskStatus status) { 
            this.status = status; }

        public String getStatusLabel() { 
            return statusLabel; }
        public void setStatusLabel(String statusLabel) { 
            this.statusLabel = statusLabel; }

        public String getStatusClass() { 
            return statusClass; }
        public void setStatusClass(String statusClass) { 
            this.statusClass = statusClass; }

        public String getTimeText() { 
            return timeText; }
        public void setTimeText(String timeText) { 
            this.timeText = timeText; }

        public String getAssignedByName() { 
            return assignedByName; }
        public void setAssignedByName(String assignedByName) { 
            this.assignedByName = assignedByName; }

        public String getResultText() { 
            return resultText; }
        public void setResultText(String resultText) { 
            this.resultText = resultText; }
    }
}