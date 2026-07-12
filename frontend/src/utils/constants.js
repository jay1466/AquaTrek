/** AquaTrack frontend constants */

export const APP_NAME = 'AquaTrack';

export const USER_ROLES = {
  SUPER_ADMIN: 'SUPER_ADMIN',
  ADMIN: 'ADMIN',
  MANAGER: 'MANAGER',
  RESIDENT: 'RESIDENT',
};

export const STATUS_COLORS = {
  ACTIVE:    'success',
  INACTIVE:  'default',
  SUSPENDED: 'warning',
  PENDING:   'warning',
  DELETED:   'error',
};

export const STATUS_LABELS = {
  ACTIVE:    'Active',
  INACTIVE:  'Inactive',
  SUSPENDED: 'Suspended',
  PENDING:   'Pending',
  DELETED:   'Deleted',
};

export const BUILDING_TYPES = [
  { value: 'RESIDENTIAL', label: 'Residential' },
  { value: 'COMMERCIAL',  label: 'Commercial'  },
  { value: 'MIXED',       label: 'Mixed Use'   },
];

export const GENDER_OPTIONS = [
  { value: 'MALE',              label: 'Male'             },
  { value: 'FEMALE',            label: 'Female'           },
  { value: 'OTHER',             label: 'Other'            },
  { value: 'PREFER_NOT_TO_SAY', label: 'Prefer not to say'},
];

export const SUBSCRIPTION_PLANS = [
  { value: 'BASIC',       label: 'Basic'       },
  { value: 'STANDARD',    label: 'Standard'    },
  { value: 'PREMIUM',     label: 'Premium'     },
  { value: 'ENTERPRISE',  label: 'Enterprise'  },
];

export const PAGINATION = {
  DEFAULT_PAGE: 0,
  DEFAULT_SIZE: 20,
  SIZE_OPTIONS: [10, 20, 50, 100],
};
