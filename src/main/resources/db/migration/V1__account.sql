-- Account: the shop's customer accounts and their roles
CREATE TABLE accounts (
  id VARCHAR(64) PRIMARY KEY,
  -- Unique because the email is the login credential; the use cases guard it as well, so this
  -- constraint is the backstop against a race, not the primary check.
  email VARCHAR(255) NOT NULL UNIQUE,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  date_of_birth DATE NOT NULL,
  -- An account is linked to exactly one UserId (ADR-011: the cross-context identity)
  linked_user_id VARCHAR(64) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  last_login_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE account_roles (
  account_id VARCHAR(64) NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
  role VARCHAR(64) NOT NULL,
  PRIMARY KEY (account_id, role)
);
