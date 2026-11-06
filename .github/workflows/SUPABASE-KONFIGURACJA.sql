-- 1) Tabela z listą administratorów
create table if not exists public.admin_users (
  user_id uuid primary key references auth.users(id) on delete cascade,
  created_at timestamptz not null default now()
);
alter table public.admin_users enable row level security;

-- Administrator może odczytać własny wpis; lista nie jest publiczna.
drop policy if exists "Admin reads own membership" on public.admin_users;
create policy "Admin reads own membership"
on public.admin_users for select to authenticated
using (user_id = auth.uid());

-- 2) Zabezpieczenie tabeli z zadaniami:
-- Każdy może odczytać tylko opublikowane zadania.
-- Tylko użytkownik dodany do admin_users może zarządzać całą bazą.
drop policy if exists "Published tasks are readable" on public.tasks;
create policy "Published tasks are readable"
on public.tasks for select to anon, authenticated
using (published = true);

drop policy if exists "Admins can read all tasks" on public.tasks;
create policy "Admins can read all tasks"
on public.tasks for select to authenticated
using (exists (select 1 from public.admin_users a where a.user_id = auth.uid()));

drop policy if exists "Admins can insert tasks" on public.tasks;
create policy "Admins can insert tasks"
on public.tasks for insert to authenticated
with check (exists (select 1 from public.admin_users a where a.user_id = auth.uid()));

drop policy if exists "Admins can update tasks" on public.tasks;
create policy "Admins can update tasks"
on public.tasks for update to authenticated
using (exists (select 1 from public.admin_users a where a.user_id = auth.uid()))
with check (exists (select 1 from public.admin_users a where a.user_id = auth.uid()));

drop policy if exists "Admins can delete tasks" on public.tasks;
create policy "Admins can delete tasks"
on public.tasks for delete to authenticated
using (exists (select 1 from public.admin_users a where a.user_id = auth.uid()));
