package com.kalibyte.YashTools.audit.entity.enums;
public enum AuditAction {

    // ── Authentication ────────────────────────────────────────────────────────
    LOGIN,
    LOGOUT,
    LOGIN_FAILED,
    PASSWORD_CHANGED,
    TOKEN_REFRESHED,

    // ── User Management ───────────────────────────────────────────────────────
    USER_CREATED,
    USER_UPDATED,
    USER_DELETED,
    USER_ENABLED,
    USER_DISABLED,
    ROLE_ASSIGNED,
    CREATE_DESIGNATION,
    GET_ALL_DESIGNATIONS,
    CREATE_SKILL,
    GET_ALL_SKILLS,
    GET_SKILL_BY_ID,
    DELETE_SKILL,
    GET_ALL_USERS,
    GET_USER_BY_ID,
    GET_USER_BY_EMAIL,
    GET_USER_BY_NAME,
    GET_USER_BY_PHONE,
    GET_USERS_BY_SKILL,
    GET_USER_BY_SKILL_ID,
    GET_USERS_BY_ROLE,

    // ── Project Management ────────────────────────────────────────────────────
    PROJECT_CREATED,
    PROJECT_UPDATED,
    PROJECT_DELETED,
    PROJECT_STATUS_CHANGED,
    PROJECT_REWORK_STARTED,
    GET_ALL_PROJECTS,
    GET_PROJECT_BY_ID,
    GET_STATUS_HISTORY,
    GET_ALL_PROJECT_MANAGERS,
    GET_ALL_PROJECT_EMPLOYEES,

    // ── Task Management ───────────────────────────────────────────────────────
    TASK_CREATED,
    TASK_UPDATED,
    TASK_STATUS_CHANGED,
    TASK_SUBMITTED,
    TASK_REVIEWED,
    GET_ALL_TASKS,
    GET_MY_TASKS,
    GET_TASK_BY_ID,
    GET_TASK_TIMELINE,
    GET_TASK_SUGGESTIONS,
    GET_TASK_ATTACHMENTS,
    DOWNLOAD_TASK_ATTACHMENT,

    // ── Candidate Management ──────────────────────────────────────────────────
    CANDIDATE_CREATED,
    CANDIDATE_UPDATED,
    CANDIDATE_DELETED,
    CANDIDATE_STATUS_UPDATED,
    CANDIDATE_SELECTED,
    CANDIDATE_REJECTED,
    CANDIDATE_ONBOARDED,

    // ── Candidate Details ─────────────────────────────────────────────────────
    CANDIDATE_DETAILS_ADDED,
    CANDIDATE_DETAILS_UPDATED,
    CANDIDATE_DETAILS_FETCHED,

    // ── Candidate Resume ──────────────────────────────────────────────────────
    CANDIDATE_RESUME_UPDATED,
    CANDIDATE_RESUME_DOWNLOADED,

    // ── Interview ─────────────────────────────────────────────────────────────
    INTERVIEW_FEEDBACK_ADDED,
    INTERVIEW_SCHEDULE_FETCHED,
    UPCOMING_INTERVIEWS_FETCHED,

    // ── Offer Letter ──────────────────────────────────────────────────────────
    OFFER_LETTER_SENT,
    OFFER_RESPONSE_RECEIVED,

    // ── Candidate Emails ──────────────────────────────────────────────────────
    CANDIDATE_SELECTION_EMAIL_SENT,
    CANDIDATE_REJECTION_EMAIL_SENT,

    // ── Candidate Queries ─────────────────────────────────────────────────────
    CANDIDATE_FETCHED,
    CANDIDATES_FETCHED,
    CANDIDATES_BY_STATUS_FETCHED,
    CANDIDATE_STATISTICS_FETCHED,
    EXPIRING_INTERNSHIPS_FETCHED,

    // ── Bulk Operations ───────────────────────────────────────────────────────
    CANDIDATES_BULK_IMPORTED,

    // ── Leave Management ──────────────────────────────────────────────────────
    LEAVE_APPLIED,
    LEAVE_APPROVED,
    LEAVE_REJECTED,
    LEAVE_CANCELLED,

    // ── Sales Management ──────────────────────────────────────────────────────
    LEAD_CREATED,
    LEAD_UPDATED,
    LEAD_DELETED,
    LEAD_CONVERTED,
    LEAD_ASSIGNED,
    LEAD_ACTIVITY_RECORDED,
    LEAD_FOLLOWUP_CREATED,
    LEAD_FOLLOWUP_COMPLETED,
    GET_LEAD_BY_ID,
    GET_MY_LEADS,
    GET_ALL_LEADS,
    GET_LEAD_ACTIVITIES,
    GET_TODAY_FOLLOWUPS,
    GET_WEEK_FOLLOWUPS,
    LEAD_IMPORT_STARTED,
    LEAD_IMPORT_COMPLETED,
    OPPORTUNITY_CREATED,
    OPPORTUNITY_UPDATED,
    OPPORTUNITY_DELETED,

    // ── Attendance Management ─────────────────────────────────────────────────
    ATTENDANCE_EXPORTED,
    MANUAL_ATTENDANCE_UPDATE,
    GET_ATTENDANCE_RECORDS,
    GET_MY_ATTENDANCE_RECORDS,
    GET_TODAY_ATTENDANCE_COUNT,

    // ── Reports ───────────────────────────────────────────────────────────────
    GET_REPORT,

    // ── Data Access ───────────────────────────────────────────────────────────
    DATA_EXPORTED,

    // ── Security ──────────────────────────────────────────────────────────────
    UNAUTHORIZED_ACCESS,
    PERMISSION_DENIED,

    // ── Audit Logs ────────────────────────────────────────────────────────────
    GET_AUDIT_LOGS,
    GET_AUDIT_LOG_BY_ID,
    GET_USER_ACTIVITY,
    GET_ENTITY_AUDIT_TRAIL,
    GET_AUDIT_STATISTICS,
    GET_TOP_ACTIVE_USERS,
    GET_RECENT_LOGS,
    EXPORT_AUDIT_LOGS,

    // ── Rate Chart ────────────────────────────────────────────────────────────
    RATE_CHART_ROD_NET_PRICE_IMPORTED,
    RATE_CHART_ROD_NET_PRICE_CREATED,
    RATE_CHART_ROD_NET_PRICE_UPDATED,
    RATE_CHART_ROD_NET_PRICE_DELETED,
    RATE_CHART_ROD_NET_PRICE_VIEWED,
    RATE_CHART_COOLANT_HOLE_ROD_IMPORTED,
    RATE_CHART_COOLANT_HOLE_ROD_CREATED,
    RATE_CHART_COOLANT_HOLE_ROD_UPDATED,
    RATE_CHART_COOLANT_HOLE_ROD_DELETED,
    RATE_CHART_COOLANT_HOLE_ROD_VIEWED,
    TOOL_SERVICE_RATE_MASTER_IMPORTED,
    TOOL_SERVICE_RATE_MASTER_CREATED,
    TOOL_SERVICE_RATE_MASTER_UPDATED,
    TOOL_SERVICE_RATE_MASTER_DELETED,
    TOOL_SERVICE_RATE_MASTER_VIEWED,

    // ── Customer Management ───────────────────────────────────────────────────
    CUSTOMER_CREATED,
    CUSTOMER_UPDATED,
    CUSTOMER_DELETED,
    CUSTOMER_VIEWED,

    // ── Enquiry Management ────────────────────────────────────────────────────
    ENQUIRY_CREATED,
    ENQUIRY_UPDATED,
    ENQUIRY_DELETED,
    ENQUIRY_STATUS_UPDATED,
    ENQUIRY_VIEWED,

    // ── Company Management ────────────────────────────────────────────────────
    COMPANY_VIEWED,

    // ── Labor Management ──────────────────────────────────────────────────────
    ADVANCE_CREATED,
    ADVANCE_VIEWED,
    ATTENDANCE_RECORDED,
    ATTENDANCE_VIEWED,
    LABORER_CREATED,
    LABORER_UPDATED,
    LABORER_VIEWED,
    PAYOUT_CREATED,
    PAYOUT_VIEWED,

    // ── Master Settings ───────────────────────────────────────────────────────
    COATING_CREATED,
    COATING_UPDATED,
    COATING_VIEWED,
    COATING_DELETED,
    RAW_MATERIAL_CREATED,
    RAW_MATERIAL_UPDATED,
    RAW_MATERIAL_VIEWED,
    RAW_MATERIAL_DELETED,

    // ── Purchase Management ───────────────────────────────────────────────────
    VENDOR_CREATED,
    VENDOR_UPDATED,
    VENDOR_DELETED,
    VENDOR_VIEWED,
    PAYMENT_TERMS_CREATED,
    PAYMENT_TERMS_UPDATED,
    PAYMENT_TERMS_DELETED,
    PAYMENT_TERMS_VIEWED,
    PURCHASE_TYPE_CREATED,
    PURCHASE_TYPE_UPDATED,
    PURCHASE_TYPE_DELETED,
    PURCHASE_TYPE_VIEWED,
    PURCHASE_ORDER_CREATED,
    PURCHASE_ORDER_UPDATED,
    PURCHASE_ORDER_DELETED,
    PURCHASE_ORDER_VIEWED,
    PURCHASE_ORDER_STATUS_CHANGED,
    GRN_CREATED,
    GRN_UPDATED,
    GRN_DELETED,
    GRN_VIEWED,
    GRN_STATUS_CHANGED,
    PURCHASE_INVOICE_CREATED,
    PURCHASE_INVOICE_UPDATED,
    PURCHASE_INVOICE_DELETED,
    PURCHASE_INVOICE_VIEWED,
    PURCHASE_RETURN_CREATED,
    PURCHASE_RETURN_UPDATED,
    PURCHASE_RETURN_DELETED,
    PURCHASE_RETURN_VIEWED,
    VENDOR_PAYMENT_CREATED,
    VENDOR_PAYMENT_UPDATED,
    VENDOR_PAYMENT_DELETED,
    VENDOR_PAYMENT_VIEWED,

    // ── Quotation ─────────────────────────────────────────────────────────────
    QUOTATION_CREATED,
    QUOTATION_UPDATED,
    QUOTATION_DELETED,
    QUOTATION_REVISED,
    QUOTATION_LOCKED,
    QUOTATION_CANCELLED,

    // ── Other ─────────────────────────────────────────────────────────────────
    OTHER;

    public String getDisplayName() {
        return this.name().replace('_', ' ');
    }
}

