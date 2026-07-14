ALTER TABLE consultations_medicales
  ADD COLUMN analyses_prescrites TEXT NULL
  COMMENT 'JSON: liste des analyses/examens liés à la consultation';
