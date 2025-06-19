import { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import ModalLayout from '../layout/ModalLayout';

export default function UpdateSceneContent() {
    const location = useLocation();
    const { id: sceneId } = useParams();
    const scene = location.state?.scene;

    // convert "3:08 pm" → "15:08"
    const convertTo24Hour = (time12h) => {
        if (!time12h) return '';
        // Split into ["3:08", "pm"]
        const [timePart, modifier] = time12h.split(' ');
        let [hours, minutes] = timePart.split(':').map(str => parseInt(str, 10));

        if (modifier.toLowerCase() === 'pm' && hours < 12) {
            hours += 12;
        }
        if (modifier.toLowerCase() === 'am' && hours === 12) {
            hours = 0;
        }
        const hh = hours.toString().padStart(2, '0');
        const mm = minutes.toString().padStart(2, '0');
        return `${hh}:${mm}`;
    };

    const initialFrom = scene?.fromTime ? convertTo24Hour(scene.fromTime) : '';
    const initialTo = scene?.toTime ? convertTo24Hour(scene.toTime) : '';

    const [deviceList, setDeviceList] = useState([]);
    const [roomList, setRoomList] = useState([]);
    const [ruleName, setRuleName] = useState(scene?.ruleName || '');
    const [fromTime, setFromTime] = useState(initialFrom);
    const [toTime, setToTime] = useState(initialTo);
    const [selectedDays, setSelectedDays] = useState(scene?.days || []);
    const [device, setDevice] = useState(scene?.thingId || '');
    const [room, setRoom] = useState(scene?.roomId || '');
    const [command, setCommand] = useState(scene?.command || 'ON');
    const [modal, setModal] = useState({ show: false, title: '', message: '', isError: false, onConfirm: null });
    const navigate = useNavigate();

    const token = localStorage.getItem('token');

    const days = [
        { short: 'Sun', full: 'SUNDAY' },
        { short: 'Mon', full: 'MONDAY' },
        { short: 'Tue', full: 'TUESDAY' },
        { short: 'Wed', full: 'WEDNESDAY' },
        { short: 'Thu', full: 'THURSDAY' },
        { short: 'Fri', full: 'FRIDAY' },
        { short: 'Sat', full: 'SATURDAY' },
    ];

    useEffect(() => {
        if (!token) {
            navigate('/');
            return;
        }
        const fetchRoomsAndDevices = async () => {
            try {
                const response1 = await axios.get(`${process.env.REACT_APP_API_URL}/user/room`,
                    { headers: { Authorization: `Bearer ${token}` } }
                );
                if (response1.status === 200) {
                    setRoomList(response1.data);
                }
            } catch (err) {
                const errorMessage = err.response?.data?.error || 'Failed to fetch room';
                console.error(errorMessage);
            }

            try {
                const response2 = await axios.get(`${process.env.REACT_APP_API_URL}/user/thing`,
                    { headers: { Authorization: `Bearer ${token}` } }
                );
                if (response2.status === 200) {
                    setDeviceList(response2.data.things);
                }
            } catch (err) {
                const errorMessage = err.response?.data?.error || 'Failed to fetch device';
                console.error(errorMessage);
            }
        };
        fetchRoomsAndDevices();
    }, [navigate, token]);

    const toggleDay = (dayFull) => {
        setSelectedDays(prev =>
            prev.includes(dayFull) ? prev.filter(d => d !== dayFull) : [...prev, dayFull]);
    };

    const handleSubmit = async () => {
        if (!token) {
            navigate('/');
            return;
        }
        if (!fromTime || !toTime || selectedDays.length === 0 || !room || !device || !command) {
            setModal({
                show: true,
                title: 'Error',
                message: <span className='text-danger'>All fields are required</span>,
                isError: true,
                onConfirm: () => setModal({ ...modal, show: false }),
            });
            return;
        }

        const payload = {
            ruleId: sceneId,
            ruleName,
            fromTime,
            toTime,
            days: selectedDays,
            roomId: room,
            roomName: roomList.find(r => r.roomId === room)?.roomName || '',
            thingId: device,
            command,
        };

        try {
            const { data, status } = await axios.patch(`${process.env.REACT_APP_API_URL}/user/rule/update`, payload,
                { headers: { Authorization: `Bearer ${token}` } }
            );
            if (status === 200) {
                setModal({
                    show: true,
                    title: 'Success',
                    message: 'Your Schedule updated successfully',
                    isError: false,
                    onConfirm: () => {
                        setModal({ ...modal, show: false });
                        navigate('/schedule/your_schedule');
                    }
                });
            }
        } catch (err) {
            const errorMessage = err.response?.data?.error || 'Failed to update schedule. Please try again.';
            setModal({
                show: true,
                title: 'Failed',
                message: <span className='text-danger'>{errorMessage}</span>,
                isError: true,
                onConfirm: () => setModal({ ...modal, show: false }),
            });
        }
    };

    const handleDelete = async () => {
        if (!token) {
            navigate('/');
            return;
        }
        const confirmDelete = window.confirm("Are you sure you want to delete this schedule?");
        if (!confirmDelete) return;
        try {
            const { data, status } = await axios.delete(`${process.env.REACT_APP_API_URL}/user/delete/rule`,
                { headers: { Authorization: `Bearer ${token}` }, params: { ruleId: sceneId } }
            );
            if (status === 200) {
                setModal({
                    show: true,
                    title: 'Success',
                    message: data.message,
                    isError: false,
                    onConfirm: () => {
                        setModal({ ...modal, show: false });
                        navigate('/schedule/your_schedule');
                    }
                });
            }
        } catch (err) {
            const errorMessage = err.response?.data?.error || 'Failed to delete schedule. Please try again.';
            setModal({
                show: true,
                title: 'Failed',
                message: <span className='text-danger'>{errorMessage}</span>,
                isError: true,
                onConfirm: () => setModal({ ...modal, show: false }),
            });
        }
    };

    return (
        <>
            <div className='container px-5 py-4'>

                <div style={{ fontSize: '24px', lineHeight: '100%', letterSpacing: '0' }} className='mb-3'>Update Schedule</div>

                {/* Content */}
                <div style={{ width: '100%', overflowX: 'hidden', height: '300px', overflowY: 'auto' }}>

                    <div className="mb-2">
                        <div className="form-label">Name</div>
                        <input type='text' id='ruleName' className="form-control" value={ruleName} onChange={e => setRuleName(e.target.value)} required />
                    </div>

                    <div className="mb-2">
                        <div className="form-label">Time</div>
                        <div className="d-flex gap-2">
                            <input type="time" id='time' className="form-control" value={fromTime} onChange={e => setFromTime(e.target.value)} required />
                            <span className="align-self-center">To</span>
                            <input type="time" id='time' className="form-control" value={toTime} onChange={e => setToTime(e.target.value)} required />
                        </div>
                    </div>

                    <div className="mb-2">
                        <div className="form-label">Days</div>
                        <div className="d-flex flex-wrap gap-2">
                            {days.map(({ short, full }) => (
                                <button
                                    key={full}
                                    className={`btn border border-dark rounded-circle d-flex justify-content-center align-items-center ${selectedDays.includes(full) ? 'btn-dark text-white' : 'btn-light'}`}
                                    onClick={() => toggleDay(full)}
                                    style={{ height: '35px', width: '35px', fontSize: '12px' }}>
                                    {short}
                                </button>
                            ))}
                        </div>
                    </div>

                    <div className="mb-2">
                        <div className="form-label">Device</div>
                        <select className="form-select" value={device} onChange={e => setDevice(e.target.value)} required>
                            <option value="">Select Device</option>
                            {deviceList.map((deviceListObj, index) => (
                                <option key={index} value={deviceListObj.thingId}>{deviceListObj.label}</option>
                            ))}
                        </select>
                    </div>

                    <div className="mb-2">
                        <label className="form-label">Room</label>
                        <select className="form-select" value={room} onChange={e => setRoom(e.target.value)} required>
                            <option value="">Select Room</option>
                            {roomList.map((roomListObj, index) => (
                                <option key={index} value={roomListObj.roomId}>{roomListObj.roomName}</option>
                            ))}
                        </select>
                    </div>

                    <div className="mb-2">
                        <div className="d-block form-label">Condition</div>
                        <button className='btn btn-dark' onClick={() => setCommand('ON')}>On</button>
                    </div>
                </div>

                <div className='d-flex justify-content-between my-5'>
                    <button className="btn btn-outline-eaeaea" onClick={handleDelete}>Delete</button>
                    <button className="btn btn-dark" onClick={handleSubmit}>Update</button>
                </div>
            </div>

            {/* Alert Modal */}
            {modal.show && (
                <ModalLayout title={modal.title} msg={modal.message} modal={modal.onConfirm} hideClose={!modal.isError}>
                    <button onClick={modal.onConfirm} className={`btn btn-dark px-3`}>
                        {modal.isError ? 'Try Again' : 'OK'}
                    </button>
                </ModalLayout>
            )}
        </>
    );
};