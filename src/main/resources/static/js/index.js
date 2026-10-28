document.addEventListener('DOMContentLoaded', () => {
    const studentsBody = document.getElementById('studentsBody');
    const filtersForm = document.getElementById('filtersForm');
    const resetButton = document.getElementById('resetFilters');
    const message = document.getElementById('listMessage');
    const addButton = document.querySelector('.button-add');

    addButton.addEventListener('click', event => {
        if (!isAdmin()) {
            event.preventDefault();
            showMessage('Добавлять и изменять студентов может только администратор');
        }
    });

    function showMessage(text) {
        message.textContent = text;
        message.hidden = !text;
    }

    async function loadStudents(filters = {}) {
        try {
            showMessage('');
            studentsBody.replaceChildren();

            const students = await getStudents(filters);
            const canEdit = isAdmin();
            addButton.setAttribute('aria-disabled', String(!canEdit));

            if (students.length === 0) {
                showMessage('Студенты не найдены');
                return;
            }

            for (const student of students) {
                const row = document.createElement('tr');

                const values = [
                    student.fullName,
                    student.groupName,
                    student.dormNumber,
                    student.room
                ];

                for (const value of values) {
                    const cell = document.createElement('td');
                    cell.textContent = value ?? '—';
                    row.appendChild(cell);
                }

                const actions = document.createElement('td');

                const view = document.createElement('a');
                view.href = `student.html?id=${student.id}`;
                view.textContent = 'Просмотр';
                view.className = 'button button-secondary';

                const edit = document.createElement('a');
                edit.href = `form.html?id=${student.id}`;
                edit.textContent = 'Изменить';
                edit.className = 'button button-secondary';
                edit.setAttribute('aria-disabled', String(!canEdit));
                edit.addEventListener('click', event => {
                    if (!isAdmin()) {
                        event.preventDefault();
                        showMessage('Изменять студентов может только администратор');
                    }
                });

                const remove = document.createElement('button');
                remove.type = 'button';
                remove.textContent = 'Удалить';
                remove.className = 'button button-secondary';
                remove.disabled = !canEdit;

                remove.addEventListener('click', async () => {
                    if (!confirm('Удалить студента?')) {
                        return;
                    }

                    try {
                        await deleteStudent(student.id);
                        await loadStudents(filters);
                    } catch (error) {
                        showMessage(error.message);
                    }
                });

                actions.append(view, edit, remove);
                row.appendChild(actions);
                studentsBody.appendChild(row);
            }
        } catch (error) {
            showMessage(error.message);
        }
    }

    filtersForm.addEventListener('submit', event => {
        event.preventDefault();

        loadStudents({
            fullName: document.getElementById('filterFullName').value,
            groupName: document.getElementById('filterGroup').value,
            isuId: document.getElementById('filterIsuId').value,
            dormNumber: document.getElementById('filterDormNumber').value,
            room: document.getElementById('filterRoom').value,
            moveInDate: document.getElementById('filterMoveInDate').value,
            isForeigner: document.getElementById('filterForeigner').value
        });
    });

    resetButton.addEventListener('click', () => {
        filtersForm.reset();
        loadStudents();
    });

    loadStudents();
});
