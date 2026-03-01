type TranslateFn = (key: string) => string;

export function statusLabel(status: string, t: TranslateFn): string {
  const map: Record<string, string> = {
    draft: "statusDraft",
    in_review: "statusInReview",
    active: "statusActive",
    archived: "statusArchived",
    published: "statusPublished",
    accepted: "statusAccepted",
    in_development: "statusInDevelopment",
    in_testing: "statusInTesting",
    done: "statusDone",
    rejected: "statusRejected",
    todo: "statusTodo",
    in_progress: "statusInProgress",
    blocked: "statusBlocked",
    pending: "statusPending",
    confirmed: "statusConfirmed",
    success: "statusSuccess",
    failed: "statusFailed"
  };
  const key = map[status];
  return key ? t(key) : `${t("statusUnknown")}: ${status}`;
}

export function statusClass(status: string): string {
  switch (status) {
    case "success":
    case "active":
      return "statusChip statusSuccess";
    case "failed":
      return "statusChip statusDanger";
    case "draft":
      return "statusChip statusDraft";
    case "in_review":
      return "statusChip statusMuted";
    case "published":
      return "statusChip statusInfo";
    case "accepted":
      return "statusChip statusSuccess";
    case "in_development":
      return "statusChip statusInfo";
    case "in_testing":
      return "statusChip statusInfo";
    case "done":
      return "statusChip statusSuccess";
    case "rejected":
      return "statusChip statusDanger";
    case "todo":
      return "statusChip statusMuted";
    case "in_progress":
      return "statusChip statusInfo";
    case "blocked":
      return "statusChip statusDanger";
    case "pending":
      return "statusChip statusMuted";
    case "confirmed":
      return "statusChip statusSuccess";
    case "archived":
      return "statusChip statusMuted";
    default:
      return "statusChip";
  }
}
